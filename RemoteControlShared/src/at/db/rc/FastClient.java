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
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.net.SocketFactory;

import at.db.net.RC4InputStream;
import at.db.net.RC4OutputStream;
import at.db.net.UdpOutputStream;
import at.db.rc.Commands.Command;
import at.db.rc.Commands.Control;
import at.db.rc.Commands.Event;
import at.db.rc.Commands.Status;

public class FastClient implements Runnable {

	private final ExecutorService				executorService			= Executors.newSingleThreadExecutor();

	public enum EndPointType {
		SERVER, CLIENT
	}

	private EndPointType					endPointType;
	private Socket								socket;

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

//	public static void assertNotRunningInMainThread() {
//		if (Thread.currentThread().getName().equals("main")) {
//			throw new IllegalStateException("Current operation must not run in main thread");
//		}
//	}
	
	public FastClient(final String serverAddress, final int serverPort, final EndPointType newEndPointType)
			throws InterruptedException, ExecutionException {
		executorService.submit(new Callable<Object>() {
			public Void call() throws Exception {
				SocketFactory socketfactory = SocketFactory.getDefault();
				socket = socketfactory.createSocket(serverAddress, serverPort);
				endPointType = newEndPointType;

				socket.setTcpNoDelay(true);
				socket.setTrafficClass(0x02 | 0x10);
				socket.setPerformancePreferences(1, 2, 0);
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
				return null;
			}
		}).get();
	}

	public FastClient(final Socket newSocket, final EndPointType newEndPointType)
			throws InterruptedException, ExecutionException {
		executorService.submit(new Callable<Object>() {
			public Void call() throws Exception {
				socket = newSocket;
				endPointType = newEndPointType;

				socket.setTcpNoDelay(true);
				socket.setTrafficClass(0x02 | 0x10);
				socket.setPerformancePreferences(1, 2, 0);
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
				return null;
			}
		}).get();
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

	private byte[] rsaEncrypt(final byte[] data) {
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

	private byte[] rsaDecrypt(final byte[] data) {
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

	public synchronized void dispatchConnect(final byte[] publicKeyHash, final String password)
			throws InterruptedException, ExecutionException {
		executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				if (encryptConnection) {
					dispatchAuthentification(publicKeyHash);
				}
		
				dos.write(Command.CONTROL | Control.CHECK_PROTOCOL_VERSION);
				dos.writeFloat(protocolVersion);
				dos.flush();
				byte command = dis.readByte();
				if ((command & (Command.MASK | Status.MASK)) != (Command.STATUS | Status.SUCCESS)) {
					// TODO: handle error
				}
		
				dos.write(Command.CONTROL | Control.CONNECT);
				dos.writeUTF(password);
		
				counter = new Random().nextInt();
				dos.writeInt(counter);
				dos.flush();
				command = dis.readByte();
				if ((command & Command.MASK) == Command.STATUS) {
					if ((command & Status.MASK) != Status.SUCCESS) {
						// TODO: handle error
					}
				}
				if (allowUdpConnection) {
					cos = new UdpOutputStream(socket.getInetAddress(), socket.getPort());
					dos = new DataOutputStream(cos);
				}
				return null;
			}
		}).get();
	}

	private void dispatchAuthentification(final byte[] publicKeyHash)
			throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		dos.write(Command.CONTROL | Control.HANDSHAKE_START);
		dos.flush();
		byte command = dis.readByte();
		if (command != (Command.MASK | Control.HANDSHAKE_PUBLIC_KEY)) {
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

	private void dispatchFinalizeHandshake(final byte[] encodedKey)
			throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		PublicKey publicKey = KeyFactory.getInstance(Parameters.KEY_ALGORITHM).generatePublic( new X509EncodedKeySpec(encodedKey));
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

	public synchronized void dispatchMouseEvent(final byte action, final int buttonMask, final float x, final float y)
			throws InterruptedException, ExecutionException {
		executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				dos.write(Command.DATA | Event.MOUSE);
				dos.writeInt(getNextCounter());
				dos.write(action);
				dos.writeInt(buttonMask);
				dos.writeFloat(x);
				dos.writeFloat(y);
				dos.flush();
				return null;
			}
		}).get();
	}

	public synchronized void dispatchKeyboardEvent(final byte action, final int keyCode)
			throws InterruptedException, ExecutionException {
		executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				dos.write(Command.DATA | Event.KEYBOARD);
				dos.writeInt(getNextCounter());
				dos.write(action);
				dos.writeInt(keyCode);
				dos.flush();
				return null;
			}
		}).get();
	}

	public synchronized void dispatchMediaControlEvent(final byte action, final int keyCode)
			throws InterruptedException, ExecutionException {
		executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				dos.write(Command.DATA | Event.MEDIA_CONTROL);
				dos.writeInt(getNextCounter());
				dos.write(action);
				dos.writeInt(keyCode);
				dos.flush();
				return null;
			}
		}).get();
	}

	public synchronized void dispatchScrollEvent(final float y)
			throws InterruptedException, ExecutionException {
		executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				dos.write(Command.DATA | Event.SCROLL);
				dos.writeInt(getNextCounter());
				dos.writeFloat(y);
				dos.flush();
				return null;
			}
		}).get();
	}

	public synchronized void dispatchAccelerationEvent(final float x, final float y, final float z)
			throws InterruptedException, ExecutionException {
		executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				dos.write(Command.DATA | Event.ACCELERATION);
				dos.writeInt(getNextCounter());
				dos.writeFloat(x);
				dos.writeFloat(y);
				dos.writeFloat(z);
				dos.flush();
				return null;
			}
		}).get();
	}

	public synchronized void dispatchTextEvent(final int action, final String text)
			throws InterruptedException, ExecutionException {
		executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				dos.write(Command.DATA | Event.TEXT);
				dos.writeInt(getNextCounter());
				dos.writeInt(action);
				dos.writeUTF(text);
				dos.flush();
				return null;
			}
		}).get();
	}

	public synchronized void disconnect()
			throws InterruptedException, ExecutionException {
		final FastClient endPoint = this;
		executorService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				dos.write(Command.CONTROL | Control.DISCONNECT);
				dos.flush();
				dis.close();
				dos.close();
				terminate = true;
				if (terminationListener != null) {
					terminationListener.onTerminated(endPoint);
				}
				return null;
			}
		}).get();
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

	public void handleInput()
			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
//		FastClient.assertNotRunningInMainThread();
		byte command = dis.readByte();
		switch (command & Command.MASK) {
		case Command.CONTROL:
			switch (command & Control.MASK) {
			case Control.HANDSHAKE_START:
				dos.write(Command.MASK | Control.HANDSHAKE_PUBLIC_KEY);

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

			case Control.CHECK_PROTOCOL_VERSION:
				// TODO check protocol version
				float version = dis.readFloat();
				if (protocolVersion == version) {
					dos.write(Command.STATUS | Status.SUCCESS);
				} else {
					dos.write(Command.STATUS | Status.WRONG_PASSWORD);
				}
				break;

			case Control.CONNECT:
				String password = dis.readUTF();
				counter = dis.readInt();
				if (expectedPassword.equals(password)) {
					dos.write(Command.STATUS | Status.SUCCESS);
				} else {
					dos.write(Command.STATUS | Status.WRONG_PASSWORD);
				}
				break;

			case Control.DISCONNECT:
				terminate = true;
				dis.close();
				dos.close();
				break;

			default:
				// TODO: HANDLE ERROR
				break;
			}
			break;
		case Command.DATA:
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
			switch (command & Event.MASK) {
			case Event.MOUSE:
				eventHandler.handleMouseEvent(dis.readByte(), dis.readInt(), dis.readFloat(), dis.readFloat());
				break;

			case Event.KEYBOARD:
				eventHandler.handleKeyboardEvent(dis.readByte(), dis.readInt());
				break;

			case Event.MEDIA_CONTROL:
				eventHandler.handleMediaControlEvent(dis.readByte(), dis.readInt());
				break;

			case Event.SCROLL:
				eventHandler.handleScrollEvent(dis.readFloat());
				break;

			case Event.ACCELERATION:
				eventHandler.handleAccelerationEvent(dis.readFloat(), dis.readFloat(), dis.readFloat());
				break;

			case Event.TEXT:
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
