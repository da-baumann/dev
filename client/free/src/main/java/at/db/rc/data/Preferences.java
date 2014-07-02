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
package at.db.rc.data;

import android.content.SharedPreferences;

public class Preferences {

	public enum MouseController {
		TOUCH_PAD, POINTER_STICK, ACCELEROMETER
	}

	public static final int	FIRST_APP_START_FLAG				= 0x01;
	private static final String	FIRST_APP_START				= "first-app-start";
	private static final String	SERVER_NAME						= "server-name";
	private static final String	SERVER_PORT						= "server-port";
	private static final String	AUTO_CONNECT					= "auto-connect";
	private static final String	ENCRYPT_CONNECTION		= "encrypt-connection";
	private static final String	ALLOW_UDP_CONNECTION	= "allow-udp-connection";
	private static final String	PASSWORD							= "password";
	private static final String	MOUSE_CONTROLLER			= "mouse-controller";
	private static final String	BUTTONS_ENABLED				= "buttons-enabled";
	private static final String	SCROLLBAR_ENABLED			= "scrollbar-enabled";
	private static final String	PUSH_TO_CLICK_ENABLED	= "push-to-click-enabled";

	private boolean							firstAppStart					= true;
	private String							serverName						= "";
	private int									serverPort						= 12121;
	private boolean							autoConnect						= true;
	private boolean							encryptConnection			= true;
	private boolean							allowUdpConnection		= false;
	private String							password							= "";
	private MouseController			mouseController				= MouseController.TOUCH_PAD;
	private boolean							buttonsEnabled				= true;
	private boolean							scrollbarEnabled			= true;
	private boolean							pushToClickEnabled		= false;

	public boolean isFirstAppStart() {
		return firstAppStart;
	}

	public void setFirstAppStart(boolean firstAppStart) {
		this.firstAppStart = firstAppStart;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public boolean isAutoConnect() {
		return autoConnect;
	}

	public void setAutoConnect(boolean autoConnect) {
		this.autoConnect = autoConnect;
	}

	public boolean isEncryptConnection() {
		return encryptConnection;
	}

	public void setEncryptConnection(boolean encryptConnection) {
		this.encryptConnection = encryptConnection;
	}

	public boolean isAllowUdpConnection() {
		return allowUdpConnection;
	}

	public void setAllowUdpConnection(boolean allowUdpConnection) {
		this.allowUdpConnection = allowUdpConnection;
	}

	public MouseController getMouseController() {
		return mouseController;
	}

	public void setMouseController(MouseController mouseController) {
		this.mouseController = mouseController;
	}

	public boolean isButtonsEnabled() {
		return buttonsEnabled;
	}

	public void setButtonsEnabled(boolean buttonsEnabled) {
		this.buttonsEnabled = buttonsEnabled;
	}

	public boolean isScrollbarEnabled() {
		return scrollbarEnabled;
	}

	public void setScrollbarEnabled(boolean scrollbarEnabled) {
		this.scrollbarEnabled = scrollbarEnabled;
	}

	public boolean isPushToClickEnabled() {
		return pushToClickEnabled;
	}

	public void setPushToClickEnabled(boolean pushToClickEnabled) {
		this.pushToClickEnabled = pushToClickEnabled;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void load(SharedPreferences settings) {
		if (settings.contains(FIRST_APP_START)) {
			setFirstAppStart(settings.getBoolean(FIRST_APP_START, true));
		}
		if (settings.contains(SERVER_NAME)) {
			setServerName(settings.getString(SERVER_NAME, ""));
		}
		if (settings.contains(SERVER_PORT)) {
			setServerPort(settings.getInt(SERVER_PORT, 0));
		}
		if (settings.contains(AUTO_CONNECT)) {
			setAutoConnect(settings.getBoolean(AUTO_CONNECT, true));
		}
		if (settings.contains(ENCRYPT_CONNECTION)) {
			setEncryptConnection(settings.getBoolean(ENCRYPT_CONNECTION, true));
		}
		if (settings.contains(ALLOW_UDP_CONNECTION)) {
			setAllowUdpConnection(settings.getBoolean(ALLOW_UDP_CONNECTION, false));
		}
		if (settings.contains(PASSWORD)) {
			setPassword(settings.getString(PASSWORD, ""));
		}
		if (settings.contains(MOUSE_CONTROLLER)) {
			setMouseController(MouseController.valueOf(settings.getString(MOUSE_CONTROLLER,
					MouseController.TOUCH_PAD.toString())));
		}
		if (settings.contains(BUTTONS_ENABLED)) {
			setButtonsEnabled(settings.getBoolean(BUTTONS_ENABLED, true));
		}
		if (settings.contains(SCROLLBAR_ENABLED)) {
			setScrollbarEnabled(settings.getBoolean(SCROLLBAR_ENABLED, true));
		}
		if (settings.contains(PUSH_TO_CLICK_ENABLED)) {
			setPushToClickEnabled(settings.getBoolean(PUSH_TO_CLICK_ENABLED, false));
		}

	}

	public void persist(SharedPreferences settings) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(FIRST_APP_START, isFirstAppStart());
		editor.putString(SERVER_NAME, getServerName());
		editor.putInt(SERVER_PORT, getServerPort());
		editor.putBoolean(AUTO_CONNECT, isAutoConnect());
		editor.putBoolean(ENCRYPT_CONNECTION, isEncryptConnection());
		editor.putBoolean(ALLOW_UDP_CONNECTION, isAllowUdpConnection());
		editor.putString(PASSWORD, getPassword());
		editor.putString(MOUSE_CONTROLLER, getMouseController().toString());
		editor.putBoolean(BUTTONS_ENABLED, isButtonsEnabled());
		editor.putBoolean(SCROLLBAR_ENABLED, isScrollbarEnabled());
		editor.putBoolean(PUSH_TO_CLICK_ENABLED, isPushToClickEnabled());
		editor.commit();
	}

	public void persist(SharedPreferences settings, int property) {
		SharedPreferences.Editor editor = settings.edit();
		if ((property & FIRST_APP_START_FLAG) == FIRST_APP_START_FLAG) {
			editor.putBoolean(FIRST_APP_START, isFirstAppStart());
		}
		editor.commit();
	}

}
