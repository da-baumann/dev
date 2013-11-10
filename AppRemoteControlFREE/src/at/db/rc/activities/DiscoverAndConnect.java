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
package at.db.rc.activities;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import at.db.net.UdpOutputStream;
import at.db.rc.A;
import at.db.rc.Client;
import at.db.rc.Parameters;
import at.db.rc.data.Preferences;

public class DiscoverAndConnect extends Activity {

	public static final String	TAG								= DiscoverAndConnect.class.getName();

	public static final byte[]	BROADCAST_ADDRESS	= { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
	private int									port							= Parameters.DEFAULT_PORT;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setTitle(A.string.lyt_discover_and_connect);
			discoverServer();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	private void discoverServer() {
		boolean errorDetected = false;
		DatagramSocket socket = null;
		String host = null;
		byte[] data = new byte[5];
		new Random().nextBytes(data);
		try {
			InetAddress broadcastAddress = InetAddress.getByAddress(BROADCAST_ADDRESS);
			socket = new DatagramSocket();
			socket.setBroadcast(true);
			UdpOutputStream outputStream = new UdpOutputStream(socket, broadcastAddress, Parameters.DEFAULT_PORT);
			data[0] = Parameters.DISCOVERY_REQUEST;
			outputStream.write(data);
			outputStream.flush();

			DatagramPacket msg = new DatagramPacket(data, data.length);
			try {
				socket.setSoTimeout(2000);
				while (host == null) {
					socket.receive(msg);

					byte[] response = msg.getData();
					if (isValidResponse(data, response)) {
						InetAddress address = msg.getAddress();
						host = address.getHostAddress();
					}

				}
			} catch (SocketException e) {
				if (!errorDetected) {
					Toast toast = Toast.makeText(getApplicationContext(), A.string.msg_error_networkt_not_reachable,
							Toast.LENGTH_LONG);
					toast.show();
					errorDetected = true;
				}
			} catch (SocketTimeoutException e) {
				Log.d(TAG, e.toString());
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			} finally {
				if (socket != null) {
					socket.close();
				}
			}

			if (host != null) {
				Preferences preferences = Master.getInstance().getPreferences();
				preferences.setServerName(host);
				preferences.setServerPort(port);
				Master.getInstance().persistPreferences();

				Client clientConnection = Master.getInstance().getClient();
				clientConnection.connect();
			} else {
				if (!errorDetected) {
					Toast toast = Toast.makeText(getApplicationContext(), A.string.msg_error_no_server_found, Toast.LENGTH_LONG);
					toast.show();
					errorDetected = true;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			finish();
		}
	}

	private boolean isValidResponse(byte[] data, byte[] response) {
		if (data.length != response.length) {
			return false;
		}
		if (response[0] != Parameters.DISCOVERY_RESPONSE) {
			return false;
		}
		for (int i = 1; i < data.length; i++) {
			if (data[i] != response[i]) {
				return false;
			}
		}
		return true;
	}

}
