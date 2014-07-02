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

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import at.db.rc.A;
import at.db.rc.Client;
import at.db.rc.controls.TextSink;
import at.db.rc.controls.TouchSink;
import at.db.rc.data.Preferences;

public class Master extends Activity {

	public static final String	TAG								= Master.class.getName();

	public static final String	PREFERENCES_NAME	= "rc-preferences";
	private static Master				instance;

	private Preferences					preferences				= new Preferences();

	private TouchSink						view;
	private TextSink						textSink;

	private Client							client;

	private SensorManager				sensorManager;

	private Sensor							accelerometer;

	public static Master getInstance() {
		return instance;
	}

	public Client getClient() {
		if (client == null) {
			client = new Client(this);
			applyPreferences(client, preferences);
		}
		return client;
	}

	/*
	 * TODO: implement bluetooth:
	 * 
	 * public static final int REQUEST_ENABLE_BT = 5; public static final UUID
	 * MY_UUID = new UUID(1, 1);
	 * 
	 * public void testBlueTooth() { BluetoothAdapter mBluetoothAdapter =
	 * BluetoothAdapter.getDefaultAdapter(); if (mBluetoothAdapter == null) { //
	 * Device does not support Bluetooth } if (!mBluetoothAdapter.isEnabled()) {
	 * Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	 * startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); }
	 * 
	 * final Vector<String> mArrayAdapter = new Vector<String>();
	 * Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
	 * // If there are paired devices if (pairedDevices.size() > 0) { // Loop
	 * through paired devices for (BluetoothDevice device : pairedDevices) { //
	 * Add the name and address to an array adapter to show in a ListView
	 * mArrayAdapter.add(device.getName() + "\n" + device.getAddress()); } }
	 * 
	 * // Create a BroadcastReceiver for ACTION_FOUND final BroadcastReceiver
	 * mReceiver = new BroadcastReceiver() { public void onReceive(Context
	 * context, Intent intent) { try { String action = intent.getAction(); // When
	 * discovery finds a device if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	 * // Get the BluetoothDevice object from the Intent BluetoothDevice device =
	 * intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); // Add the name
	 * and address to an array adapter to show in a // ListView if
	 * (device.getAddress().equals("C0:CB:38:AC:46:F8")) {
	 * System.out.println(device.getBluetoothClass()); BluetoothSocket socket =
	 * device.createRfcommSocketToServiceRecord(MY_UUID); socket.connect();
	 * OutputStream os = socket.getOutputStream();
	 * 
	 * } mArrayAdapter.add(device.getName() + "\n" + device.getAddress()); }
	 * System.out.println(); } catch (Exception e) { e.printStackTrace(); } } };
	 * // Register the BroadcastReceiver IntentFilter filter = new
	 * IntentFilter(BluetoothDevice.ACTION_FOUND); registerReceiver(mReceiver,
	 * filter); // Don't forget to unregister during // onDestroy
	 * mBluetoothAdapter.startDiscovery();
	 * 
	 * }
	 */

	private void applyPreferences(Client c, Preferences pref) {
		if ((c != null) && (pref != null)) {
			c.setServerAddress(pref.getServerName());
			c.setServerPort(pref.getServerPort());
			c.setAutoConnect(pref.isAutoConnect());
			c.setEncryptConnection(pref.isEncryptConnection());
			c.setAllowUdpConnection(pref.isAllowUdpConnection());
			c.setPassword(pref.getPassword());
			c.setMouseController(pref.getMouseController(), pref.isButtonsEnabled(), pref.isScrollbarEnabled());
			c.setPushToClickEnabled(pref.isPushToClickEnabled());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			Uri data = getIntent().getData();
			instance = this;
			super.onCreate(savedInstanceState);
			setTitle(A.string.lyt_remote_control);
			loadPreferences(data);

			setContentView(A.layout.main);

			showServerAppLink(false);

			view = (TouchSink) findViewById(A.id.viewMousePad);
			view.setOnTouchListener(getClient());

			textSink = (TextSink) findViewById(A.id.btnToggleKeyboard);
			textSink.setTextListener(getClient());

			sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(getClient(), accelerometer, SensorManager.SENSOR_DELAY_GAME);

			applyPreferences(client, preferences);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	private void showServerAppLink(boolean force) {
		if (force || preferences.isFirstAppStart()) {
			new AlertDialog.Builder(this).setTitle(A.string.msg_question_server_file_downloaded_header)
					.setMessage(A.string.msg_question_server_file_downloaded_message).setCancelable(false)
					.setPositiveButton(A.string.msg_question_result_proceed, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							preferences.setFirstAppStart(false);
						}
					}).show();
		}
	}

	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(getClient(), accelerometer, SensorManager.SENSOR_DELAY_GAME);
	}

	protected void onPause() {
		sensorManager.unregisterListener(getClient());
		super.onPause();
	}

	@Override
	protected void onStop() {
		try {
			persistPreferences();
			super.onStop();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	public Preferences getPreferences() {
		return preferences;
	}

	public void loadPreferences(Uri uri) {
		try {
			SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, 0);
			preferences.load(settings);
			if (uri != null) {
				List<String> params = uri.getPathSegments();
				String host = params.get(0);
				Integer port = Integer.parseInt(params.get(1));
				// String keyMd5Sum = params.get(2);
				if ((host != null) && (port != null)) {
					preferences.setServerName(host);
					preferences.setServerPort(port);
					// preferences.setKeyMd5Sum(keyMd5Sum);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	public void persistPreferences() {
		try {
			SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, 0);
			preferences.persist(settings);
			applyPreferences(client, preferences);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		try {
			MenuItem mni = menu.findItem(A.id.mniConnection);
			if (getClient().isConnected()) {
				mni.setTitle(A.string.mni_main_disconnect);
			} else {
				mni.setTitle(A.string.mni_main_connect);
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			return false;
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(A.menu.main_menu, menu);
			return true;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			case A.id.mniDiscoverAndConnect:
				return startActivity(DiscoverAndConnect.class);
			case A.id.mniScanAndConnect:
				return startActivity(ScanAndConnect.class);
			case A.id.mniSettings:
				return startActivity(SettingsEditor.class);
			case A.id.mniWriteServerFile:
				return startActivity(ServerFileWriter.class);
			case A.id.mniConnection:
				return startActivity(Connector.class);
			default:
				return super.onOptionsItemSelected(item);
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return false;
	}

	public boolean startActivity(Class<? extends Activity> activityClass) {
		try {
			Intent i = new Intent(this, activityClass);
			startActivity(i);
			return true;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return false;
	}

}
