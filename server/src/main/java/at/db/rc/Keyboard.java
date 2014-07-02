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

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import at.db.rc.keyboard.IKeyboardLayout;
import at.db.rc.keyboard.KeyboardLayoutLinuxEn;

public class Keyboard {

	private Robot						robot;
	private IKeyboardLayout	keyboardLayout;

	public Keyboard() throws AWTException {
		this.robot = new Robot();
		this.keyboardLayout = new KeyboardLayoutLinuxEn();
	}

	public Keyboard(Robot robot) {
		this.robot = robot;
	}

	public Keyboard(IKeyboardLayout keyboardLayout) throws AWTException {
		this.robot = new Robot();
		this.keyboardLayout = keyboardLayout;
	}

	public Keyboard(Robot robot, IKeyboardLayout keyboardLayout) {
		this.robot = robot;
		this.keyboardLayout = keyboardLayout;
	}

	public void setKeyboardLayout(IKeyboardLayout keyboardLayout) {
		this.keyboardLayout = keyboardLayout;
	}

	public void type(CharSequence characters) {
		int length = characters.length();
		for (int i = 0; i < length; i++) {
			char character = characters.charAt(i);
			type(character);
		}
	}

	public void type(char character) {
		doType(keyboardLayout.getKeyCodes(character));
	}

	public void doType(int... keyCodes) {
		try {
			doType(keyCodes, 0, keyCodes.length);
		} catch (Exception e) {
			System.out.println(" ... invalid keys: " + getKeyCodesAsText(keyCodes));
		}
	}

	private String getKeyCodesAsText(int[] keyCodes) {
		StringBuffer sb = new StringBuffer();
		for (int i : keyCodes) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(i);
		}
		return sb.toString();
	}

	private void doType(int[] keyCodes, int offset, int length) {
		if (length == 0) {
			return;
		}

		try {
			press(keyCodes[offset]);
			doType(keyCodes, offset + 1, length - 1);
		} catch (Exception e) {
			System.out.println(" ... invalid keys: " + getKeyCodesAsText(keyCodes));
		} finally {
			release(keyCodes[offset]);
		}
	}

	private void press(int keyCode) {
		robot.keyPress(keyCode);
	}

	private void release(int keyCode) {
		robot.keyRelease(keyCode);
	}

	public void pressRaw(int keyCode) {
		Integer translation = getTranslation(keyCode);
		if (translation != null) {
			robot.keyPress(translation);
		}
	}

	public void releaseRaw(int keyCode) {
		Integer translation = getTranslation(keyCode);
		if (translation != null) {
			robot.keyRelease(translation);
		}
	}

	private Integer getTranslation(int keyCode) {
		switch (keyCode) {
		case 19:
			return KeyEvent.VK_UP;
		case 20:
			return KeyEvent.VK_DOWN;
		case 21:
			return KeyEvent.VK_LEFT;
		case 22:
			return KeyEvent.VK_RIGHT;
		case 66:
			return KeyEvent.VK_ENTER;
		case 67:
			return KeyEvent.VK_BACK_SPACE;
		case 82:
			return KeyEvent.VK_ALT;
		default:
			System.err.println("can not translate " + keyCode);
			return null;
			// return KeyEvent.VK_ALT;
			// default:
			// throw new IllegalArgumentException("Cannot type character " + keyCode);
		}
	}

}
