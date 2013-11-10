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

import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.SocketFactory;

import android.content.Context;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import at.db.os.Const;
import at.db.rc.FastClient.EndPointType;
import at.db.rc.activities.Master;
import at.db.rc.controls.TouchSink;
import at.db.rc.data.Preferences.MouseController;
import at.db.rc.interfaces.IRegionProvider;
import at.db.rc.interfaces.IRegionProvider.Region;
import at.db.rc.interfaces.ITextListener;

public class Client implements ITextListener, SensorEventListener, ITerminationListener {

	public static final String					TAG									= Client.class.getName();
	private final ITerminationListener	terminationListener	= this;
	private final ExecutorService				executorService			= Executors.newSingleThreadExecutor();

	private String											serverAddress				= null;
	private int													serverPort					= 0;
	private boolean											autoConnect					= true;
	private boolean											encryptConnection		= true;
	private boolean											allowUdpConnection	= false;
	private int													highPressureBit			= 0;

	private String											password						= null;
	private MouseController							mouseController			= MouseController.TOUCH_PAD;
	private IRegionProvider							regionProvider;

	private FastClient									endPoint						= null;
	private Region											downRegion					= null;
	private Master											master							= null;

	private long												lastConnectionError	= 0;

	private long												lastSensorChangeTime;

	public Client(Master master) {
		this.master = master;
	}

	public synchronized void connect() {
		try {
			executorService.submit(new Callable<Object>() {
				public Object call() throws Exception {
					if (!isConnected()) {
						SocketFactory socketfactory = SocketFactory.getDefault();
						Socket socket = socketfactory.createSocket(getServerAddress(), getServerPort());
						endPoint = new FastClient(socket, EndPointType.CLIENT);
						endPoint.setEncryptConnection(encryptConnection);
						endPoint.setAllowUdpConnection(allowUdpConnection);
						endPoint.dispatchConnect(null, password);
						endPoint.setTerminationListener(terminationListener);
					}
					return null;
				}
			}).get();
		} catch (Exception e) {
			if ((System.currentTimeMillis() - lastConnectionError) > 5000) {
				String message = master.getString(A.string.msg_error_could_not_connect_to_server) + "\n" + e.getMessage();
				Toast.makeText(master, message, Toast.LENGTH_LONG).show();
				Log.i(TAG, e.toString());
				lastConnectionError = System.currentTimeMillis();
			}
		}
	}

	public boolean testConnect() {
		try {
			return executorService.submit(new Callable<Boolean>() {
				public Boolean call() throws Exception {
						if (isConnected()) {
							disconnect();
						}
						SocketFactory socketfactory = SocketFactory.getDefault();
						Socket socket = socketfactory.createSocket(getServerAddress(), getServerPort());
						endPoint = new FastClient(socket, EndPointType.CLIENT);
						endPoint.setEncryptConnection(encryptConnection);
						endPoint.dispatchConnect(null, password);
						endPoint.setTerminationListener(terminationListener);
						disconnect();
						return true;
				}
			}).get();
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}
		return false;
	}

	@Override
	public void onTerminated(FastClient endPoint) {
		endPoint = null;
	}

	public void disconnect() {
		try {
			endPoint.disconnect();
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}
		endPoint = null;
	}

	public boolean isConnected() {
		if (endPoint != null) {
			return endPoint.isConnected();
		}
		return false;
	}

	/* == Socket Properties ================== */

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public void setAutoConnect(boolean autoConnect) {
		this.autoConnect = autoConnect;
	}

	public void setEncryptConnection(boolean encryptConnection) {
		this.encryptConnection = encryptConnection;
	}

	public void setAllowUdpConnection(boolean allowUdpConnection) {
		this.allowUdpConnection = allowUdpConnection;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	public void setMouseController(MouseController mouseController, boolean buttonsEnabled, boolean scrollbarEnabled) {
		if (regionProvider != null) {
			this.mouseController = mouseController;
			int touchSinkModes = 0;
			if (buttonsEnabled) {
				touchSinkModes |= TouchSink.BUTTONS;
			}
			if (scrollbarEnabled) {
				touchSinkModes |= TouchSink.SCROLL_PAD;
			}
			switch (mouseController) {
			case TOUCH_PAD:
				touchSinkModes |= TouchSink.TOUCH_PAD;
				break;
			case POINTER_STICK:
				touchSinkModes |= TouchSink.POINTER_STICK;
				break;
			default:
				break;
			}
			regionProvider.setTouchSinkModes(touchSinkModes);
		}
	}

	public void setPushToClickEnabled(boolean pushToClickEnabled) {
		if (pushToClickEnabled) {
			highPressureBit = Const.HIGH_PRESSURE;
		} else {
			highPressureBit = 0;
		}
	}

	@Override
	public boolean updateText(CharSequence text) {
		if (!assertConnection()) {
			return true;
		}
		try {
			endPoint.dispatchTextEvent(0, text.toString());
			return true;
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}
		return false;
	}

	@Override
	public boolean action(int action, int keyCode) {
		if (!assertConnection()) {
			return true;
		}
		try {
			endPoint.dispatchKeyboardEvent((byte) action, keyCode);
			return true;
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}
		return false;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (!assertConnection()) {
			return true;
		}
		try {
			InputMethodManager imm = (InputMethodManager) master.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(master.findViewById(A.id.btnToggleKeyboard).getWindowToken(), 0);

			Region region = regionProvider.getRegion(event.getX(), event.getY());
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				downRegion = region;
			}

			if (downRegion == Region.LEFT_BUTTON) {
				endPoint.dispatchMouseEvent((byte) event.getAction(), Const.BUTTON1_MASK, event.getX(), event.getY());

			} else if (downRegion == Region.RIGHT_BUTTON) {
				endPoint.dispatchMouseEvent((byte) event.getAction(), Const.BUTTON3_MASK, event.getX(), event.getY());

			} else if (downRegion == Region.SCROLL_PAD) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					endPoint.dispatchScrollEvent(-1);
				} else {
					endPoint.dispatchScrollEvent(event.getY());
				}

			} else if (downRegion == Region.TOUCH_PAD) {
				int mask = Const.TOUCH_PAD;
				if (event.getPressure() > 0.35) {
					mask |= highPressureBit;
				}
				int action = event.getAction();
				float x = event.getX();
				float y = event.getY();
				endPoint.dispatchMouseEvent((byte) action, mask, x, y);

			} else if (downRegion == Region.POINTER_STICK) {
				int mask = Const.POINTER_STICK;
				if (event.getPressure() > 0.35) {
					mask |= highPressureBit;
				}
				int action = event.getAction();
				PointF point = regionProvider.getPointerStickPoint(event.getX(), event.getY());

				endPoint.dispatchMouseEvent((byte) action, mask, point.x, point.y);
			}

			if (event.getAction() == MotionEvent.ACTION_UP) {
				downRegion = null;
			}
			return true;

		} catch (SocketException e) {
			disconnect();
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}
		return false;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (isControlKey(keyCode)) {
			return false;
		}

		if (!assertConnection()) {
			return true;
		}
		try {
			switch (keyCode) {

			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (assertConnection()) {
					endPoint.dispatchKeyboardEvent((byte) event.getAction(), keyCode);
				}
				return true;

			case KeyEvent.KEYCODE_DPAD_CENTER:
				if (assertConnection()) {
					endPoint.dispatchMouseEvent((byte) event.getAction(), Const.BUTTON1_MASK, -1, -1);
				}
				return true;

			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				if (assertConnection()) {
					endPoint.dispatchMediaControlEvent((byte) event.getAction(), keyCode);
				}
				return true;

			default:
				if (assertConnection()) {
					endPoint.dispatchKeyboardEvent((byte) event.getAction(), keyCode);
				}
				return true;

			}
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}
		return false;
	}

	private boolean isControlKey(int keyCode) {
		return (keyCode == KeyEvent.KEYCODE_BACK) || (keyCode == KeyEvent.KEYCODE_MENU);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int change) {
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		try {
			if (mouseController == MouseController.ACCELEROMETER) {
				if (System.nanoTime() - lastSensorChangeTime > 50000000) {
					if (!isConnected()) {
						return;
					}
					float x = sensorEvent.values[0];
					float y = sensorEvent.values[1];
					float z = sensorEvent.values[2];
					endPoint.dispatchAccelerationEvent(x, y, z);
					lastSensorChangeTime = System.nanoTime();
				}
			}
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}
	}

	private synchronized boolean assertConnection() {
		if (isConnected()) {
			return true;
		} else if (autoConnect) {
			connect();
		}
		return isConnected();
	}

	public void setRegionProvider(IRegionProvider regionProvider) {
		this.regionProvider = regionProvider;
	}

}
