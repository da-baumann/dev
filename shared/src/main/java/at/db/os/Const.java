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
package at.db.os;

public class Const {

	public static final int	TOUCH_PAD					= 1 << 6;																		// 64;
	public static final int	POINTER_STICK			= 1 << 7;																		// 128;

	public static final int	HIGH_PRESSURE			= 1 << 5;																		// 32;

	public static final int	BUTTON1_DOWN_MASK	= 1 << 10;																		// 1024;
	public static final int	BUTTON1_MASK			= 1 << 4;																		// 16;
	public static final int	BUTTON2_DOWN_MASK	= 1 << 11;																		// 2048;
	public static final int	BUTTON2_MASK			= 1 << 3;																		// 8;
	public static final int	BUTTON3_DOWN_MASK	= 1 << 12;																		// 4096;
	public static final int	BUTTON3_MASK			= 1 << 2;																		// 4;

	public static final int	ANY_BUTTON				= BUTTON1_MASK | BUTTON2_MASK | BUTTON3_MASK;
	
}
