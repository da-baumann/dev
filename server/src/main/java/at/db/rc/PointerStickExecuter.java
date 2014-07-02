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
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;

public class PointerStickExecuter implements Runnable {

	private Robot		robot			= null;
	private boolean	terminate	= false;
	private float		x					= 0;
	private float		y					= 0;
	private Double	mouseX		= null;
	private Double	mouseY		= null;

	public PointerStickExecuter(Robot robot) throws AWTException {
		if (robot == null) {
			this.robot = new Robot();
		} else {
			this.robot = robot;
		}
	}

	@Override
	public void run() {
		terminate = false;
		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		Point pointerLocation = pointerInfo.getLocation();
		mouseX = pointerLocation.getX();
		mouseY = pointerLocation.getY();
		while (!terminate) {
			try {
				mouseX += (x * Math.abs(x) * 100);
				mouseY += (y * Math.abs(y) * 100);
				robot.mouseMove(new Double(mouseX).intValue(), new Double(mouseY).intValue());
				Thread.sleep(10);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		mouseX = null;
		mouseY = null;
	}

	public void setPoint(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void setTerminate(boolean terminate) {
		this.terminate = terminate;
	}

}
