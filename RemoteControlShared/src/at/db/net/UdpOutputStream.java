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
package at.db.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UdpOutputStream extends OutputStream {

	byte[]									buffer	= null;
	private DatagramSocket	socket;
	private InetAddress	address;
	private int	port;

	public UdpOutputStream(InetAddress address, int port) throws SocketException {
		this.address = address;
		this.port = port;
	}

	public UdpOutputStream(DatagramSocket socket, InetAddress address, int port) {
		this.socket = socket;
		this.address = address;
		this.port = port;
	}

	public void assertBuffer() {
		if (buffer == null) {
			buffer = new byte[Byte.MAX_VALUE];
			buffer[0] = 0;
		}
	}

	public void assertSocket() throws SocketException {
		if (socket == null) {
			socket = new DatagramSocket();
		}
	}

	@Override
	public void write(int b) throws IOException {
		assertBuffer();
		int nextPsition = buffer[0] + 1;
		buffer[nextPsition] = (byte) b;
		buffer[0] = (byte) (nextPsition);
		if ((buffer[0] + 1) >= Byte.MAX_VALUE) {
			flush();
		}
	}

	@Override
	public void flush() throws IOException {
		assertBuffer();
		if (buffer[0] > 0) {
			DatagramPacket packet = new DatagramPacket(buffer, buffer[0] + 1, address, port);
			assertSocket();
			socket.send(packet);
			buffer = null;
		}
	}

	@Override
	public void close() throws IOException {
		if (socket != null) {
			socket.close();
			socket = null;
		}
	}

}
