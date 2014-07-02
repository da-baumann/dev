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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import at.db.rc.A;
import at.db.rc.Client;

public class Connector extends Activity {

	public static final String	TAG	= Connector.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setTitle(A.string.lyt_connector);

			Client connection = Master.getInstance().getClient();
			if (connection.isConnected()) {
				connection.disconnect();
			} else {
				connection.connect();
			}
			finish();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

}
