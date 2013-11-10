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
import static at.db.rc.Debugger.trace;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;

import at.db.os.AudioControlUtil;
import at.db.os.Const;
import at.db.rc.keyboard.IKeyboardLayout;

public class ClientHandler implements IEventHandler {

	public enum ExecuteResult {
		SUCCESS, ERROR, TERMINATE
	}

	public static final int				ACTION_DOWN					= 0;
	public static final int				ACTION_UP						= 1;
	public static final int				ACTION_MOVE					= 2;

	private Keyboard							keyboard;

	private Float									lastX								= null;
	private Float									lastY								= null;
	private Float									lastScrollY					= null;
	private Robot									robot								= null;
	private Long									lastActionDownTime	= null;
	private Long									lastClickEventTime	= null;

	private PointerStickExecuter	pse									= null;
	private boolean								lastHighPressure		= false;

	private Keyboard getKeyboard() {
		debug("ClientHandler.getKeyboard()");
		if (keyboard == null) {
			debug("creating new keyborad");
			keyboard = new Keyboard(getRobot());
		}
		return keyboard;
	}

	public void setKeyboardLayout(IKeyboardLayout keyboardLayout) {
		debug("ClientHandler.setKeyboardLayout(" + keyboardLayout + ")");
		getKeyboard().setKeyboardLayout(keyboardLayout);
	}

	private void moveMouse(float dx, float dy) {
		PointerInfo a = MouseInfo.getPointerInfo();
		Point b = a.getLocation();
		double x = b.getX();
		double y = b.getY();
		getRobot()
				.mouseMove(new Double(x + (dx * Math.abs(dx))).intValue(), new Double(y + (dy * Math.abs(dy))).intValue());
	}

	private Robot getRobot() {
		if (robot == null) {
			try {
				robot = new Robot();
			} catch (AWTException e) {
			}
		}
		return robot;
	}

	@Override
	public void handleMouseEvent(byte action, int buttonMask, float x, float y) {
		if ((buttonMask & Const.ANY_BUTTON) == buttonMask) {
			try {
				Robot robot = new Robot();
				if (action == ACTION_DOWN) {
					robot.mousePress(buttonMask);
				} else if (action == ACTION_UP) {
					robot.mouseRelease(buttonMask);
				}
			} catch (AWTException e) {
				// TODO handle exception
			}
			return;
		}
		if (lastActionDownTime != null) {
			if (action == ACTION_UP) {
				if ((System.nanoTime() - lastActionDownTime) < 200000000) {
					try {
						Robot robot = new Robot();
						robot.mousePress(Const.BUTTON1_MASK);
						robot.mouseRelease(Const.BUTTON1_MASK);
						lastClickEventTime = System.nanoTime();
						return;
					} catch (AWTException e) {
						// TODO handle exception
					}
				}
			}
		}

		if ((buttonMask & Const.TOUCH_PAD) == Const.TOUCH_PAD) {
			boolean highPressure = ((buttonMask & Const.HIGH_PRESSURE) == Const.HIGH_PRESSURE);
			if (lastHighPressure != highPressure) {
				lastHighPressure = highPressure;
				if (highPressure) {
					robot.mousePress(Const.BUTTON1_MASK);
					lastClickEventTime = System.nanoTime();
				}
			}

			if ((action == ACTION_MOVE) && (lastX != null) && (lastY != null)) {
				float diffX = x - lastX;
				float diffY = y - lastY;
				moveMouse(diffX, diffY);
			}
			if (action == ACTION_UP) {
				lastX = null;
				lastY = null;
				lastActionDownTime = null;
				if (lastClickEventTime != null) {
					robot.mouseRelease(Const.BUTTON1_MASK);
					lastClickEventTime = null;
				}
			} else {
				lastX = x;
				lastY = y;
				if (action == ACTION_DOWN) {
					lastActionDownTime = System.nanoTime();
					if ((lastClickEventTime != null) && ((System.nanoTime() - lastClickEventTime) < 200000000)) {
						robot.mousePress(Const.BUTTON1_MASK);
					} else {
						lastClickEventTime = null;
					}
				}
			}
		}

		if ((buttonMask & Const.POINTER_STICK) == Const.POINTER_STICK) {
			boolean highPressure = ((buttonMask & Const.HIGH_PRESSURE) == Const.HIGH_PRESSURE);
			if (lastHighPressure != highPressure) {
				lastHighPressure = highPressure;
				if (highPressure) {
					robot.mousePress(Const.BUTTON1_MASK);
					lastClickEventTime = System.nanoTime();
				}
			}

			PointerStickExecuter pointerStickExecuter = getPointerStickExecuter();
			if (action == ACTION_DOWN) {
				lastActionDownTime = System.nanoTime();
				pointerStickExecuter.setPoint(x, y);
				new Thread(pointerStickExecuter).start();
			} else if (action == ACTION_MOVE) {
				pointerStickExecuter.setPoint(x, y);
			} else if (action == ACTION_UP) {
				pointerStickExecuter.setPoint(0, 0);
				pointerStickExecuter.setTerminate(true);
				lastActionDownTime = null;
				if (lastClickEventTime != null) {
					robot.mouseRelease(Const.BUTTON1_MASK);
					lastClickEventTime = null;
				}
			}
		}
	}

	private synchronized PointerStickExecuter getPointerStickExecuter() {
		if (this.pse == null) {
			try {
				this.pse = new PointerStickExecuter(getRobot());
			} catch (AWTException e) {
				// TODO handle exception
			}
		}
		return this.pse;
	}

	@Override
	public void handleKeyboardEvent(byte action, int keyCode) {
		if (action == 0) {
			getKeyboard().pressRaw(keyCode);
		} else {
			getKeyboard().releaseRaw(keyCode);
		}
	}

	@Override
	public void handleMediaControlEvent(byte action, int keyCode) {
		switch (keyCode) {
		case 24:
			AudioControlUtil.adjustMasterVolume(0.05F);
			break;

		case 25:
			AudioControlUtil.adjustMasterVolume(-0.05F);
			break;

		default:
			break;
		}
	}

	@Override
	public void handleScrollEvent(float y) {
		trace("scrolling [" + y + "]");
		if (lastScrollY != null) {
			if ((y - lastScrollY) > 0) {
				getRobot().mouseWheel(1);
			} else if ((y - lastScrollY) < 0) {
				getRobot().mouseWheel(-1);
			}
		}
		if (y < 0) {
			lastScrollY = null;
		} else {
			lastScrollY = y;
		}
	}

	@Override
	public void handleAccelerationEvent(float x, float y, float z) {
		trace(x + " - " + y);
		moveMouse(-x * Math.abs(x), y * Math.abs(y));
	}

	@Override
	public void handleTextEvent(int action, String text) {
		getKeyboard().type(text);
	}

}
