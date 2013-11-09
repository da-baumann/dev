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

import at.db.rc.free.R;

public final class A {

	public static final class array {
		public static final int	settings_arr_mouse_controller	= R.array.settings_arr_mouse_controller;
	}

	public static final class drawable {
		public static final int	remote_control							= R.drawable.remote_control;
		public static final int	remote_control_connected		= R.drawable.remote_control_connected;
		public static final int	remote_control_disconnected	= R.drawable.remote_control_disconnected;
	}

	public static final class attr {
	}

	public static final class id {
		public static final int	btnScanSettings						= R.id.btnScanSettings;
		public static final int	btnTestServer							= R.id.btnTestServer;
		public static final int	btnToggleKeyboard					= R.id.btnToggleKeyboard;
		public static final int	chkAllowUdpConnection			= R.id.chkAllowUdpConnection;
		public static final int	chkAutoConnect						= R.id.chkAutoConnect;
		public static final int	chkButtonsEnabled					= R.id.chkButtonsEnabled;
		public static final int	chkEncryptConnection			= R.id.chkEncryptConnection;
		public static final int	chkPushToClickEnabled			= R.id.chkPushToClickEnabled;
		public static final int	chkScrollbarEnabled				= R.id.chkScrollbarEnabled;
		public static final int	layout_main								= R.id.layout_main;
		public static final int	mniConnection							= R.id.mniConnection;
		public static final int	mniDiscoverAndConnect			= R.id.mniDiscoverAndConnect;
		public static final int	mniScanAndConnect					= R.id.mniScanAndConnect;
		public static final int	mniSettings								= R.id.mniSettings;
		public static final int	mniSettingsDiscardChanges	= R.id.mniSettingsDiscardChanges;
		public static final int	mniSettingsResetDefaults	= R.id.mniSettingsResetDefaults;
		public static final int	mniSettingsSaveAndConnect	= R.id.mniSettingsSaveAndConnect;
		public static final int	mniWriteServerFile				= R.id.mniWriteServerFile;
		public static final int	spnMouseController				= R.id.spnMouseController;
		public static final int	txtServerName							= R.id.txtServerName;
		public static final int	txtServerPort							= R.id.txtServerPort;
		public static final int	viewMousePad							= R.id.viewMousePad;
	}

	public static final class layout {
		public static final int	main			= R.layout.main;
		public static final int	settings	= R.layout.settings;
	}

	public static final class menu {
		public static final int	main_menu			= R.menu.main_menu;
		public static final int	settings_menu	= R.menu.settings_menu;
	}

	public static final class raw {
		public static final int	remote_control_server	= R.raw.remote_control_server;
	}

	public static final class string {
		public static final int	app_name																		= R.string.app_name;
		public static final int	lyt_connector																= R.string.lyt_connector;
		public static final int	lyt_discover_and_connect										= R.string.lyt_discover_and_connect;
		public static final int	lyt_remote_control													= R.string.lyt_remote_control;
		public static final int	lyt_scan_and_connect												= R.string.lyt_scan_and_connect;
		public static final int	lyt_settings_editor													= R.string.lyt_settings_editor;
		public static final int	lyt_write_server_file												= R.string.lyt_write_server_file;
		public static final int	main_mni_connect														= R.string.main_mni_connect;
		public static final int	main_mni_discover_amp_connect								= R.string.main_mni_discover_amp_connect;
		public static final int	main_mni_scan_amp_connect										= R.string.main_mni_scan_amp_connect;
		public static final int	main_mni_settings														= R.string.main_mni_settings;
		public static final int	main_mni_write_server_file									= R.string.main_mni_write_server_file;
		public static final int	main_txt_use_keyboard												= R.string.main_txt_use_keyboard;
		public static final int	mni_main_connect														= R.string.mni_main_connect;
		public static final int	mni_main_disconnect													= R.string.mni_main_disconnect;
		public static final int	msg_error_could_not_connect_to_server				= R.string.msg_error_could_not_connect_to_server;
		public static final int	msg_error_networkt_not_reachable						= R.string.msg_error_networkt_not_reachable;
		public static final int	msg_error_no_server_found										= R.string.msg_error_no_server_found;
		public static final int	msg_error_not_enough_space									= R.string.msg_error_not_enough_space;
		public static final int	msg_error_sd_not_writable										= R.string.msg_error_sd_not_writable;
		public static final int	msg_info_result_ok													= R.string.msg_info_result_ok;
		public static final int	msg_progress_connecting											= R.string.msg_progress_connecting;
		public static final int	msg_progress_connection_ok									= R.string.msg_progress_connection_ok;
		public static final int	msg_progress_could_not_connect							= R.string.msg_progress_could_not_connect;
		public static final int	msg_progress_testing_connection							= R.string.msg_progress_testing_connection;
		public static final int	msg_question_replace_file										= R.string.msg_question_replace_file;
		public static final int	msg_question_result_no											= R.string.msg_question_result_no;
		public static final int	msg_question_result_proceed									= R.string.msg_question_result_proceed;
		public static final int	msg_question_result_yes											= R.string.msg_question_result_yes;
		public static final int	msg_question_server_file_downloaded_header	= R.string.msg_question_server_file_downloaded_header;
		public static final int	msg_question_server_file_downloaded_message	= R.string.msg_question_server_file_downloaded_message;
		public static final int	msg_success_server_file_wrtten							= R.string.msg_success_server_file_wrtten;
		public static final int	msg_title_save_server_app										= R.string.msg_title_save_server_app;
		public static final int	settings_btn_reset													= R.string.settings_btn_reset;
		public static final int	settings_btn_scan_settings									= R.string.settings_btn_scan_settings;
		public static final int	settings_btn_test_server										= R.string.settings_btn_test_server;
		public static final int	settings_chk_allow_udp_connection						= R.string.settings_chk_allow_udp_connection;
		public static final int	settings_chk_auto_connect										= R.string.settings_chk_auto_connect;
		public static final int	settings_chk_buttons_enabled								= R.string.settings_chk_buttons_enabled;
		public static final int	settings_chk_encrypt_connection							= R.string.settings_chk_encrypt_connection;
		public static final int	settings_chk_push_to_click_enabled					= R.string.settings_chk_push_to_click_enabled;
		public static final int	settings_chk_scrollbar_enabled							= R.string.settings_chk_scrollbar_enabled;
		public static final int	settings_grp_connection											= R.string.settings_grp_connection;
		public static final int	settings_grp_mouse_control									= R.string.settings_grp_mouse_control;
		public static final int	settings_grp_server													= R.string.settings_grp_server;
		public static final int	settings_lbl_scan_settings									= R.string.settings_lbl_scan_settings;
		public static final int	settings_lbl_server_port										= R.string.settings_lbl_server_port;
		public static final int	settings_mni_discard_changes								= R.string.settings_mni_discard_changes;
		public static final int	settings_mni_reset_defaults									= R.string.settings_mni_reset_defaults;
		public static final int	settings_mni_save_and_connect								= R.string.settings_mni_save_and_connect;
		public static final int	settings_txt_experimental										= R.string.settings_txt_experimental;
		public static final int	settings_txt_key_hash												= R.string.settings_txt_key_hash;
		public static final int	settings_txt_password												= R.string.settings_txt_password;
		public static final int	settings_txt_server_name										= R.string.settings_txt_server_name;
	}

}
