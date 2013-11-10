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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;
import at.db.rc.A;

public class ServerFileWriter extends Activity {

	public static final String	TAG	= ServerFileWriter.class.getName();

	private File								directory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setTitle(A.string.lyt_write_server_file);
			writeServerAppToSD();
		} catch (Exception e) {
			Log.e(TAG, e.toString(), e);
		}
	}

	private void writeServerAppToSD() {
		if (!isExternalStorageWritable()) {
			showInfo(getString(A.string.msg_error_sd_not_writable));
			return;
		}
		directory = Environment.getExternalStorageDirectory();
		final InputStream inputStream = getSourceStream();

		try {
			final File targetFile = new File(directory, "RemoteControlServer.jar");
			if (targetFile.exists()) {
				OnClickListener dialogClickListener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == DialogInterface.BUTTON_POSITIVE) {
							copyFile(inputStream, targetFile);
						}
						finish();
					}
				};
	
				Builder builder = new Builder(this);
				builder.setMessage(A.string.msg_question_replace_file)
						.setPositiveButton(A.string.msg_question_result_yes, dialogClickListener)
						.setNegativeButton(A.string.msg_question_result_no, dialogClickListener).show();
				return;
			} else {
				copyFile(inputStream, targetFile);
				finish();
			}
    } finally {
      if (inputStream != null) {
      	try {
					inputStream.close();
				} catch (IOException e) {
					Log.e(TAG, "Error on closing input stream", e);
				}
      } 
    }
	}

  private class DownloadTask extends AsyncTask<Void, Void, InputStream> {
		@Override
		protected InputStream doInBackground(Void... params) {
			InputStream inputStream = null;
			try {
				URL url = new URL("http://www.daniel-baumann.at/RemoteControlServer.jar");
				URLConnection ucon = url.openConnection();
				inputStream = ucon.getInputStream();
			} catch (IOException ignore) {
				// we don't have a connection so we will use the local file if available
			}
			return inputStream;
		}
  }
  
	private InputStream getSourceStream() {
		InputStream inputStream = null;
		ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
      AsyncTask<Void,Void,InputStream> execute = new DownloadTask().execute();
      try {
				inputStream = execute.get();
			} catch (Exception e) {
				Log.e(TAG, e.toString(), e);
			}
	  }
    
		if (inputStream == null) {
			inputStream = getResources().openRawResource(A.raw.remote_control_server);
		}
		return inputStream;
	}

	private void showInfo(String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(getString(A.string.msg_title_save_server_app));
		alertDialog.setMessage(message);
		alertDialog.setButton(getString(A.string.msg_info_result_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alertDialog.show();
	}

	private void copyFile(InputStream inputStream, File targetFile) {
		try {
			OutputStream outputStream = new FileOutputStream(targetFile);
			byte[] buffer = new byte[1024];
			int offset;
			while ((offset = inputStream.read(buffer)) > 0) {
				StatFs stat = new StatFs(directory.getPath());
				int blockSize = stat.getBlockSize();
				int availableBlocks = stat.getAvailableBlocks();
				if (offset > (blockSize * availableBlocks)) {
					showInfo(getString(A.string.msg_error_not_enough_space));
					break;
				}
				outputStream.write(buffer, 0, offset);
			}
			outputStream.close();
			inputStream.close();
			String message = getString(A.string.msg_success_server_file_wrtten) + "\n" + targetFile;
			Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
			toast.show();
		} catch (Exception e) {
			showInfo(e.getMessage());
		}
	}

	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

}
