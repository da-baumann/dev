/*
 * Copyright 2013 Daniel Baumann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package at.db.rc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

import javax.crypto.Cipher;

import at.db.net.RC4InputStream;
import at.db.net.RC4OutputStream;
import at.db.net.UdpOutputStream;

public class FastClient implements Runnable {

  /*

  Structure:
    Type: 2 bit

  00...... CONTROL_COMMAND
  10...... DATA_COMMAND
  01...... STATUS_COMMAND
  ....0000 Type: CONTROL | EVENT | STATUS

  0000.... 0
  0001.... 1
  0010.... 2
  0011.... 3
  0100.... 4
  0101.... 5
  0110.... 6
  0111.... 7
  1000.... 8
  1001.... 9
  1010.... a
  1011.... b
  1100.... c
  1101.... d
  1110.... e
  1111.... f

   */

	public enum EndPointType {
		SERVER, CLIENT
	}

	private EndPointType					endPointType;
	private Socket								socket;

	public static final byte			NONE													= 0; // (byte) 0b0000_0000

	public static final byte			COMMAND_MASK									= -64;	// (byte) 0b1100_0000; // 0xc.
	public static final byte			CONTROL_COMMAND								= 0;	// (byte) 0b0000_0000; // 0x0.
	public static final byte			DATA_COMMAND									= -128;	// (byte) 0b1000_0000; // 0x8.
	public static final byte			STATUS_COMMAND								= 64;	// (byte) 0b0100_0000; // 0x4.

	public static final byte			CONTROL_MASK									= 7;	// (byte) 0b0000_0111; // 0x.7
	public static final byte			HANDSHAKE_START_CONTROL				= 0x00;	// (byte) 0b0000_0000; // 0x.0
	public static final byte			HANDSHAKE_PUBLIC_KEY_CONTROL	= 0x01;	// (byte) 0b0000_0001; // 0x.1
	public static final byte			CHECK_PROTOCOL_VERSION				= 0x02;	// (byte) 0b0000_0010; // 0x.2
	public static final byte			CONNECT												= 0x03;	// (byte) 0b0000_0011; // 0x.3
	public static final byte			DISCONNECT										= 0x04;	// (byte) 0b0000_0100; // 0x.4

	public static final byte			EVENT_MASK										= 7;	// (byte) 0b0000_0111; // 0x.7
	public static final byte			MOUSE_EVENT										= 0x00;	// (byte) 0b0000_0000; // 0x.0
	public static final byte			KEYBOARD_EVENT								= 0x01;	// (byte) 0b0000_0001; // 0x.1
	public static final byte			MEDIA_CONTROL_EVENT						= 0x02;	// (byte) 0b0000_0010; // 0x.2
	public static final byte			SCROLL_EVENT									= 0x03;	// (byte) 0b0000_0011; // 0x.3
	public static final byte			ACCELERATION_EVENT						= 0x04;	// (byte) 0b0000_0100; // 0x.4
	public static final byte			TEXT_EVENT										= 0x05;	// (byte) 0b0000_0101; // 0x.5

	public static final byte			STATUS_MASK										= 7;	// (byte) 0b0000_0111; // 0x.7
	public static final byte			SUCCESS												= 0x00;	// (byte) 0b0000_0000; // 0x.0
	public static final byte			ERROR													= 0x01;	// (byte) 0b0000_0001; // 0x.1
	public static final byte			WRONG_PASSWORD								= 0x02;	// (byte) 0b0000_0010; // 0x.2

	private DataInputStream				dis;
	private DataOutputStream			dos;
	private IEventHandler					eventHandler;

	private float									protocolVersion								= 1.0F;

	private boolean								terminate											= false;

	private boolean								encryptConnection							= true;
	private boolean								allowUdpConnection						= false;
	private String								expectedPassword							= "";

	private byte[]								rc4Key												= new byte[16];

	private int										counter;
	private int										counterErrors									= 0;
	private KeyPair								keypair;
	private Cipher								encryptCipher;
	private Cipher								decryptCipher;
	private OutputStream					cos;
	private InputStream						cis;
	private ITerminationListener	terminationListener						= null;

	public FastClient(Socket socket, EndPointType endPointType) throws IOException {
		this.socket = socket;
		this.endPointType = endPointType;

		this.socket.setTcpNoDelay(true);
		this.socket.setTrafficClass(0x02 | 0x10);
		this.socket.setPerformancePreferences(1, 2, 0);
		dis = new DataInputStream(this.socket.getInputStream());
		dos = new DataOutputStream(this.socket.getOutputStream());
	}

	public Socket getSocket() {
		return socket;
	}

	public IEventHandler getEventHandler() {
		return eventHandler;
	}

	public void setEventHandler(IEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	private byte[] rsaEncrypt(byte[] data) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			byte[] buffer = new byte[Parameters.RSA_KEY_LENGTH / 8 - 11];
			int numRead = 0;
			while ((numRead = bais.read(buffer)) >= 0) {
				byte[] encrypted = encryptCipher.doFinal(buffer, 0, numRead);
				if (encrypted != null) {
					dos.writeInt(encrypted.length);
					dos.write(encrypted);
				}
			}
			return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private byte[] rsaDecrypt(byte[] data) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bais);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (dis.available() > 0) {
				int encryptedLength = dis.readInt();
				byte[] encrypted = new byte[encryptedLength];
				dis.readFully(encrypted);
				byte[] decrypted = decryptCipher.doFinal(encrypted);
				if (decrypted != null) {
					baos.write(decrypted);
				}
			}
			return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public synchronized void dispatchConnect(byte[] publicKeyHash, String password) throws Exception {
		if (encryptConnection) {
			dispatchAuthentification(publicKeyHash);
		}

		dos.write(CONTROL_COMMAND | CHECK_PROTOCOL_VERSION);
		dos.writeFloat(protocolVersion);
		dos.flush();
		byte command = dis.readByte();
		if ((command & (COMMAND_MASK | STATUS_MASK)) != (STATUS_COMMAND | SUCCESS)) {
			// TODO: handle error
		}

		dos.write(CONTROL_COMMAND | CONNECT);
		dos.writeUTF(password);

		counter = new Random().nextInt();
		dos.writeInt(counter);
		dos.flush();
		command = dis.readByte();
		if ((command & COMMAND_MASK) == STATUS_COMMAND) {
			if ((command & STATUS_MASK) != SUCCESS) {
				// TODO: handle error
			}
		}
		if (allowUdpConnection) {
			cos = new UdpOutputStream(socket.getInetAddress(), socket.getPort());
			dos = new DataOutputStream(cos);
		}
	}

	public synchronized void dispatchAuthentification(byte[] publicKeyHash) throws Exception {
		dos.write(CONTROL_COMMAND | HANDSHAKE_START_CONTROL);
		dos.flush();
		byte command = dis.readByte();
		if (command != (COMMAND_MASK | HANDSHAKE_PUBLIC_KEY_CONTROL)) {
			throw new IOException("Expecting public key, but command dis " + command);
		}
		int encodedKeyLength = dis.readUnsignedShort();
		byte[] encodedKey = new byte[encodedKeyLength];
		try {
			dis.readFully(encodedKey);
		} catch (Exception e) {
			// TODO handle if not read fully
			throw new IOException(e.getMessage());
		}
		if ((publicKeyHash != null) && (publicKeyHash.length > 0)) {
			// TODO: get hash for public key
			// TODO check hash against 'publicKeyHash'
		}
		dispatchFinalizeHandshake(encodedKey);
	}

	private void dispatchFinalizeHandshake(byte[] encodedKey) throws Exception {

		PublicKey publicKey = KeyFactory.getInstance(Parameters.KEY_ALGORITHM).generatePublic(
				new X509EncodedKeySpec(encodedKey));
		encryptCipher = Cipher.getInstance(Parameters.CIPHER_TRANSFORMATION);
		encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dosBuffer = new DataOutputStream(baos);

		new Random().nextBytes(rc4Key);
		dosBuffer.writeInt(rc4Key.length);
		dosBuffer.write(rc4Key);

		byte[] data = baos.toByteArray();
		byte[] encrypted = rsaEncrypt(data);
		dos.writeInt(encrypted.length);
		dos.write(encrypted);

		cis = new RC4InputStream(socket.getInputStream(), rc4Key);
		dis = new DataInputStream(cis);
		cos = new RC4OutputStream(socket.getOutputStream(), rc4Key);
		dos = new DataOutputStream(cos);
	}

	public int getNextCounter() {
		counter = (counter + 1);
		return counter;
	}

	public synchronized void dispatchMouseEvent(byte action, int buttonMask, float x, float y) throws Exception {
		dos.write(DATA_COMMAND | MOUSE_EVENT);
		dos.writeInt(getNextCounter());
		dos.write(action);
		dos.writeInt(buttonMask);
		dos.writeFloat(x);
		dos.writeFloat(y);
		dos.flush();
	}

	public synchronized void dispatchKeyboardEvent(byte action, int keyCode) throws Exception {
		dos.write(DATA_COMMAND | KEYBOARD_EVENT);
		dos.writeInt(getNextCounter());
		dos.write(action);
		dos.writeInt(keyCode);
		dos.flush();
	}

	public synchronized void dispatchMediaControlEvent(byte action, int keyCode) throws IOException {
		dos.write(DATA_COMMAND | MEDIA_CONTROL_EVENT);
		dos.writeInt(getNextCounter());
		dos.write(action);
		dos.writeInt(keyCode);
		dos.flush();
	}

	public synchronized void dispatchScrollEvent(float y) throws IOException {
		dos.write(DATA_COMMAND | SCROLL_EVENT);
		dos.writeInt(getNextCounter());
		dos.writeFloat(y);
		dos.flush();
	}

	public synchronized void dispatchAccelerationEvent(float x, float y, float z) throws IOException {
		dos.write(DATA_COMMAND | ACCELERATION_EVENT);
		dos.writeInt(getNextCounter());
		dos.writeFloat(x);
		dos.writeFloat(y);
		dos.writeFloat(z);
		dos.flush();
	}

	public synchronized void dispatchTextEvent(int action, String text) throws IOException {
		dos.write(DATA_COMMAND | TEXT_EVENT);
		dos.writeInt(getNextCounter());
		dos.writeInt(action);
		dos.writeUTF(text);
		dos.flush();
	}

	public synchronized void disconnect() throws IOException {
		dos.write(CONTROL_COMMAND | DISCONNECT);
		dos.flush();
		dis.close();
		dos.close();
		terminate = true;
		if (terminationListener != null) {
			terminationListener.onTerminated(this);
		}
	}

	public synchronized boolean isConnected() {
		return !socket.isClosed();
	}

	@Override
	public void run() {
		while (!terminate) {
			try {
				handleInput();
			} catch (Exception e) {
				terminate = true;
				try {
					disconnect();
				} catch (Exception e1) {
					// TODO: handle exception
				}
				// TODO: handle exception
			}
		}
		if (terminationListener != null) {
			terminationListener.onTerminated(this);
		}
	}

	public void handleInput() throws Exception {
		byte command = dis.readByte();
		switch (command & COMMAND_MASK) {
		case CONTROL_COMMAND:
			switch (command & CONTROL_MASK) {
			case HANDSHAKE_START_CONTROL:
				dos.write(COMMAND_MASK | HANDSHAKE_PUBLIC_KEY_CONTROL);

				PrivateKey privateKey = keypair.getPrivate();
				PublicKey publicKey = keypair.getPublic();

				decryptCipher = Cipher.getInstance(Parameters.CIPHER_TRANSFORMATION);
				decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

				byte[] encodedPublicKey = publicKey.getEncoded();
				dos.writeShort(encodedPublicKey.length);
				dos.write(encodedPublicKey);
				dos.flush();

				int encryptedLength = dis.readInt();
				byte[] encrypted = new byte[encryptedLength];
				dis.readFully(encrypted);

				byte[] decrypted = rsaDecrypt(encrypted);
				ByteArrayInputStream bais = new ByteArrayInputStream(decrypted);
				DataInputStream disBuffer = new DataInputStream(bais);

				int rc4KeyLength = disBuffer.readInt();
				byte[] rc4Key = new byte[rc4KeyLength];
				disBuffer.readFully(rc4Key);

				cis = new RC4InputStream(socket.getInputStream(), rc4Key);
				dis = new DataInputStream(cis);
				cos = new RC4OutputStream(socket.getOutputStream(), rc4Key);
				dos = new DataOutputStream(cos);
				break;

			case CHECK_PROTOCOL_VERSION:
				// TODO check protocol version
				float version = dis.readFloat();
				if (protocolVersion == version) {
					dos.write(STATUS_COMMAND | SUCCESS);
				} else {
					dos.write(STATUS_COMMAND | WRONG_PASSWORD);
				}
				break;

			case CONNECT:
				String password = dis.readUTF();
				counter = dis.readInt();
				if (expectedPassword.equals(password)) {
					dos.write(STATUS_COMMAND | SUCCESS);
				} else {
					dos.write(STATUS_COMMAND | WRONG_PASSWORD);
				}
				break;

			case DISCONNECT:
				terminate = true;
				dis.close();
				dos.close();
				break;

			default:
				// TODO: HANDLE ERROR
				break;
			}
			break;
		case DATA_COMMAND:
			int partnerCounter = dis.readInt();
			int nextMin = (counter + 1);
			int nextMax = (counter + 10);

			if (((nextMax < nextMin) && ((partnerCounter < nextMin) && (partnerCounter > nextMax)))
					|| ((nextMax > nextMin) && ((partnerCounter < nextMin) || (partnerCounter > nextMax)))) {
				counterErrors++;
				if (counterErrors >= 10) {
					throw new IOException("too many counter errors");
				}
			} else {
				counterErrors = 0;
				counter = partnerCounter;
			}
			switch (command & EVENT_MASK) {
			case MOUSE_EVENT:
				eventHandler.handleMouseEvent(dis.readByte(), dis.readInt(), dis.readFloat(), dis.readFloat());
				break;

			case KEYBOARD_EVENT:
				eventHandler.handleKeyboardEvent(dis.readByte(), dis.readInt());
				break;

			case MEDIA_CONTROL_EVENT:
				eventHandler.handleMediaControlEvent(dis.readByte(), dis.readInt());
				break;

			case SCROLL_EVENT:
				eventHandler.handleScrollEvent(dis.readFloat());
				break;

			case ACCELERATION_EVENT:
				eventHandler.handleAccelerationEvent(dis.readFloat(), dis.readFloat(), dis.readFloat());
				break;

			case TEXT_EVENT:
				eventHandler.handleTextEvent(dis.readInt(), dis.readUTF());
				break;

			default:
				// TODO: HANDLE ERROR
				break;
			}
			break;
		default:
			// TODO: HANDLE ERROR
			break;
		}
	}

	public void setKeypair(KeyPair keypair) {
		this.keypair = keypair;
	}

	public void setEncryptConnection(boolean encryptConnection) {
		this.encryptConnection = encryptConnection;
	}

	public void setAllowUdpConnection(boolean allowUdpConnection) {
		this.allowUdpConnection = allowUdpConnection;
	}

	public void setTerminationListener(ITerminationListener terminationListener) {
		this.terminationListener = terminationListener;
	}

	public EndPointType getEndPointType() {
		return endPointType;
	}

}
