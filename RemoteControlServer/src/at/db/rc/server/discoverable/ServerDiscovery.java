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
package at.db.rc.server.discoverable;

import static at.db.rc.Debugger.debug;
import static at.db.rc.Debugger.error;
import static at.db.rc.Debugger.info;
import static at.db.rc.Debugger.trace;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import at.db.rc.Parameters;

public class ServerDiscovery implements ISocketRunnable<DatagramSocket> {

	private DatagramSocket	socket					= null;
	private UdpClient				udpCLient				= null;
	private Set<Address>		addressList			= new HashSet<Address>();
	private Object					addressListLock	= new Object();

	public ServerDiscovery(DatagramSocket socket, UdpClient udpClient) {
		debug("ServerDiscovery.ServerDiscovery()");
		this.socket = socket;
		this.udpCLient = udpClient;
	}

	public void addAddress(byte[] address) {
		synchronized (addressListLock) {
			addressList.add(new Address(address));
		}
	}

	public void removeAddress(byte[] address) {
		synchronized (addressListLock) {
			addressList.remove(new Address(address));
		}
	}

	public boolean containsAddress(byte[] address) {
		synchronized (addressListLock) {
			return addressList.contains(new Address(address));
		}
	}

	@Override
	public void run() {
		while (!socket.isClosed()) {
			trace("ServerDiscovery.run()");
			try {
				DatagramPacket dp = new DatagramPacket(new byte[Byte.MAX_VALUE], Byte.MAX_VALUE);
				socket.receive(dp);
				byte[] data = extractData(dp.getData());
				if /* ((data.length == 5) && */(data[0] == Parameters.DISCOVERY_REQUEST)/* ) */{
					data[0] = Parameters.DISCOVERY_RESPONSE;
					dp.setData(data);
					socket.send(dp);
				} else if (udpCLient != null) {
					if (containsAddress(dp.getAddress().getAddress())) {
						udpCLient.handle(data);
					} else {
						debug("ServerDiscovery.run(): udp client not permitted to handle action from " + dp.getAddress());
					}
				}
			} catch (Exception e) {
				error(e);
			}
		}
		info("ServerDiscovery.run() >> thread exit");
	}

	private byte[] extractData(byte[] data) {
		trace("ServerDiscovery.extractData()");
		// TODO start - remove after client update
		if (data[0] == Parameters.DISCOVERY_REQUEST) {
			byte[] result = new byte[5];
			for (int i = 0; i < result.length; i++) {
				result[i] = data[i];
			}
			return result;
		}
		// TODO end - remove after client update

		byte[] result = new byte[data[0]];
		for (int i = 0; i < result.length; i++) {
			result[i] = data[i + 1];
		}
		return result;
	}

	@Override
	public DatagramSocket getSocket() {
		debug("ServerDiscovery.getSocket()");
		return socket;
	}

	private class Address {
		private Integer	hashCode	= null;
		private byte[]	address		= null;

		public Address(byte[] address) {
			if (address != null) {
				this.address = address.clone();
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Address)) {
				return false;
			}
			Address other = (Address) obj;
			return (other.hashCode() == this.hashCode());
		}

		@Override
		public int hashCode() {
			if (hashCode == null) {
				hashCode = Arrays.hashCode(address);
			}
			return hashCode;
		}

	}

}
