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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import at.db.rc.A;
import at.db.rc.Client;
import at.db.rc.data.Preferences;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanAndConnect extends Activity {

	public static final String	TAG	= ScanAndConnect.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setTitle(A.string.lyt_scan_and_connect);
			scanSettings();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	private void scanSettings() {
		/**
		 * @see http
		 *      ://stackoverflow.com/questions/8340875/integrate-zxing-qr-code-
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
				if (scanResult.getContents() == null) {
					finish();
					return;
				}
				Uri uri = Uri.parse(scanResult.getContents());
				if (uri != null) {
					List<String> params = uri.getPathSegments();
					String host = params.get(0);
					Integer port = Integer.parseInt(params.get(1));
					// String keyMd5Sum = params.get(2);
					if ((host != null) && (port != null)) {
						Preferences preferences = Master.getInstance().getPreferences();
						preferences.setServerName(host);
						preferences.setServerPort(port);
						// preferences.setKeyMd5Sum(keyMd5Sum);
						Master.getInstance().persistPreferences();

						Client clientConnection = Master.getInstance().getClient();
						clientConnection.connect();
						finish();
					}
				}
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	}

}
