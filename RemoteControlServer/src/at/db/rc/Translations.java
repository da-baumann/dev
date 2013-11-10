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

import static at.db.rc.Debugger.debug;
import static at.db.rc.Debugger.error;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Properties;

public class Translations {

	public static void init() {
		String translationName = "/" + System.getProperty("user.language") + ".messages";
		URL resource = Translations.class.getResource(translationName);
		if (resource == null) {
			debug(translationName + " not found ... using /default.messages");
			resource = Translations.class.getResource("/default.messages");
		}
		if (resource != null) {
			try {
				debug("reading translations from " + resource.getPath());
				Properties properties = new Properties();
				properties.load(new InputStreamReader(resource.openStream(), "UTF-8"));
				Field[] fields = Translations.class.getFields();
				debug("applying translations");
				for (Field field : fields) {
					if (properties.containsKey(field.getName())) {
						try {
							field.set(null, properties.get(field.getName()));
						} catch (Exception e) {
							error(e);
						}
					}
				}
				debug("translations applied");
			} catch (Exception e) {
				error(e);
			}
		}
	}

	public static String	MSG_DESC_REMOTE_CONTROL_SETTINGS_SCAN_SETTINGS	= "RemoteControl -> Settings -> Scan Settings";
	public static String	MSG_ERROR_AN_ERROR_OCCURED											= "An Error occured";
	public static String	MSG_ERROR_COULD_NOT_CREATE_CONNECTION_URL				= "Could not create connection URL";
	public static String	MSG_INFO_0_CONNECTIONS													= "Remote Control (0 Connections)";
	public static String	MSG_INFO_1_CONNECTION														= "Remote Control (1 Connection)";
	public static String	MSG_INFO_N_CONNECTIONS													= "Remote Control (%s Connections)";
	public static String	MSG_INFO_APP_STARTING_UP												= "App starting up!";
	public static String	MSG_ERROR_PORT_IN_USE														= "Can't start - port in use!";
	public static String	MNI_CLOSE																				= "Close";
	public static String	MNI_SHOW_SETTINGS_QR_CODE												= "Show Settings QR-Code";
	public static String	MNI_INSTALL_ANDROID_APP													= "Install Android App";
	public static String	MNI_KEYBOARD_DEUTSCH														= "Deutsch";
	public static String	MNI_KEYBOARD_ENGLISH														= "English";
	public static String	MNI_KEYBOARD_LANGUAGE														= "Keyboard Language";
	public static String	BTN_CLOSE																				= "Close";

}
