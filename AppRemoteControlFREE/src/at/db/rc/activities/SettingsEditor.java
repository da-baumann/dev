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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import at.db.rc.A;
import at.db.rc.Client;
import at.db.rc.data.Preferences;
import at.db.rc.data.Preferences.MouseController;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class SettingsEditor extends Activity {

	public static final String	TAG	= SettingsEditor.class.getName();

	private Button							btnScanSettings;
	private EditText						txtServerName;
	private EditText						txtServerPort;
	private Button							btnTestServer;
	private CheckBox						chkAutoConnect;
	private CheckBox						chkEncryptConnection;
	private CheckBox						chkAllowUdpConnection;
	// private EditText txtPassword;
	private Spinner							spnMouseController;
	private CheckBox						chkButtonsEnabled;
	private CheckBox						chkScrollbarEnabled;
	private CheckBox						chkPushToClickEnabled;

	// private Button btnResetSettings;

	private Preferences					preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(A.string.lyt_settings_editor);
		setContentView(A.layout.settings);

		preferences = Master.getInstance().getPreferences();

		btnScanSettings = (Button) findViewById(A.id.btnScanSettings);
		btnScanSettings.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				scanSettings();
			}
		});

		txtServerName = (EditText) findViewById(A.id.txtServerName);
		txtServerPort = (EditText) findViewById(A.id.txtServerPort);
		btnTestServer = (Button) findViewById(A.id.btnTestServer);
		btnTestServer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				testConnection();
			}
		});

		chkAutoConnect = (CheckBox) findViewById(A.id.chkAutoConnect);
		chkEncryptConnection = (CheckBox) findViewById(A.id.chkEncryptConnection);
		chkAllowUdpConnection = (CheckBox) findViewById(A.id.chkAllowUdpConnection);
		// txtPassword = (EditText) findViewById(R.id.txtPassword);

		spnMouseController = (Spinner) findViewById(A.id.spnMouseController);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, A.array.settings_arr_mouse_controller,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnMouseController.setAdapter(adapter);

		chkButtonsEnabled = (CheckBox) findViewById(A.id.chkButtonsEnabled);
		chkScrollbarEnabled = (CheckBox) findViewById(A.id.chkScrollbarEnabled);
		chkPushToClickEnabled = (CheckBox) findViewById(A.id.chkPushToClickEnabled);

		// btnResetSettings = (Button) findViewById(R.id.btnResetSettings);
		// btnResetSettings.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View view) {
		// loadSettings();
		// }
		// });

		loadSettings();

		btnScanSettings.setFocusableInTouchMode(true);
		btnScanSettings.requestFocus();
	}

	private void loadSettings() {
		txtServerName.setText(preferences.getServerName());
		txtServerPort.setText(Integer.toString(preferences.getServerPort()));
		chkAutoConnect.setChecked(preferences.isAutoConnect());
		chkEncryptConnection.setChecked(preferences.isEncryptConnection());
		chkAllowUdpConnection.setChecked(preferences.isAllowUdpConnection());
		// txtPassword.setText(preferences.getPassword());
		spnMouseController.setSelection(preferences.getMouseController().ordinal());
		chkButtonsEnabled.setChecked(preferences.isButtonsEnabled());
		chkScrollbarEnabled.setChecked(preferences.isScrollbarEnabled());
		chkPushToClickEnabled.setChecked(preferences.isPushToClickEnabled());
	}

	private void loadDefaults() {
		txtServerName.setText("");
		txtServerPort.setText("12121");
		chkAutoConnect.setChecked(true);
		chkEncryptConnection.setChecked(true);
		chkAllowUdpConnection.setChecked(false);
		// txtPassword.setText("");
		spnMouseController.setSelection(MouseController.TOUCH_PAD.ordinal());
		chkButtonsEnabled.setChecked(true);
		chkScrollbarEnabled.setChecked(true);
		chkPushToClickEnabled.setChecked(false);
	}

	@Override
	protected void onPause() {
		saveSettings();
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(A.menu.settings_menu, menu);
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
			case A.id.mniSettingsResetDefaults:
				loadDefaults();

				return true;
			case A.id.mniSettingsDiscardChanges:
				loadSettings();
				finish();

				return true;
			case A.id.mniSettingsSaveAndConnect:
				saveSettings();
				Client clientConnection = Master.getInstance().getClient();
				clientConnection.connect();
				finish();

				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return false;
	}

	private void scanSettings() {
		/**
		 * @see http://stackoverflow.com/questions/8340875/integrate-zxing-qr-code-
		 *      scanner-without-installing-barcode-scanner
		 */
		// Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		// intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		// startActivityForResult(intent, 0);
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		/**
		 * @see http://stackoverflow.com/questions/8340875/integrate-zxing-qr-code-
		 *      scanner-without-installing-barcode-scanner
		 */
		// if (requestCode == 0) {
		// if (resultCode == RESULT_OK) {
		// String contents = intent.getStringExtra("SCAN_RESULT");
		// Uri uri = Uri.parse(contents);
		// loadPreferences(uri);
		// String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
		// // Handle successful scan
		// } else if (resultCode == RESULT_CANCELED) {
		// // Handle cancel
		// }
		// }

		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null) {
			try {
				Uri uri = Uri.parse(scanResult.getContents());
				if (uri != null) {
					List<String> params = uri.getPathSegments();
					String host = params.get(0);
					Integer port = Integer.parseInt(params.get(1));
					// String keyMd5Sum = params.get(2);
					if ((host != null) && (port != null)) {
						txtServerName.setText(host);
						txtServerPort.setText(port.toString());
						// preferences.setKeyMd5Sum(keyMd5Sum);
					}
				}
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	}

	private void testConnection() {
		try {
			String serverName;
			int serverPort;
			try {
				serverName = txtServerName.getText().toString();
				serverPort = Integer.parseInt(txtServerPort.getText().toString());
			} catch (Exception e) {
				Log.e("Setting", "exception ... some data was incorect!");
				return;
			}
			final Client clientConnection = Master.getInstance().getClient();
			clientConnection.setServerAddress(serverName);
			clientConnection.setServerPort(serverPort);

			ProgressDialog dialog = ProgressDialog.show(this, getString(A.string.msg_progress_testing_connection),
					getString(A.string.msg_progress_connecting), true, false);
			boolean success = clientConnection.testConnect();
			dialog.dismiss();
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle(getString(A.string.msg_progress_testing_connection));
			alertDialog.setMessage(success ? getString(A.string.msg_progress_connection_ok)
					: getString(A.string.msg_progress_could_not_connect));
			alertDialog.setButton(getString(A.string.msg_info_result_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			alertDialog.show();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	protected void saveSettings() {
		preferences.setServerName(txtServerName.getText().toString());
		preferences.setServerPort(Integer.parseInt(txtServerPort.getText().toString()));
		preferences.setAutoConnect(chkAutoConnect.isChecked());
		preferences.setEncryptConnection(chkEncryptConnection.isChecked());
		preferences.setAllowUdpConnection(chkAllowUdpConnection.isChecked());
		// preferences.setPassword(txtPassword.getText().toString());
		preferences.setMouseController(Preferences.MouseController.values()[spnMouseController.getSelectedItemPosition()]);
		preferences.setButtonsEnabled(chkButtonsEnabled.isChecked());
		preferences.setScrollbarEnabled(chkScrollbarEnabled.isChecked());
		preferences.setPushToClickEnabled(chkPushToClickEnabled.isChecked());
		Master.getInstance().persistPreferences();
	}

}
