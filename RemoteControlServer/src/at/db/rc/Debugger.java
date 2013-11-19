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

import java.io.File;
import java.io.PrintStream;

public class Debugger {

	public static final int			NONE	= 0;
	public static final int			FATAL	= 1;
	public static final int			ERROR	= 2;
	public static final int			WARN	= 3;
	public static final int			INFO	= 4;
	public static final int			DEBUG	= 5;
	public static final int			TRACE	= 6;

	private static int					level	= ERROR;
	private static PrintStream	out;

	public static void init(int level, boolean useLogFile) {
		Debugger.level = level;
		if (level > NONE) {
			if (useLogFile) {
				try {
					File parent = new File(System.getProperty("user.home"));
					if (parent.exists()) {
						File file = new File(parent, System.currentTimeMillis() + ".log");
						out = new PrintStream(file);
					}
				} catch (Exception ignore) {
				}
			}
			if (out == null) {
				out = System.out;
			}
		}
	}

	public static void error(String message) {
		if (ERROR <= level) {
			write("[ERROR] ", message);
		}
	}

	public static void error(Throwable throwable) {
		if (ERROR <= level) {
			StringBuffer buffer = new StringBuffer(throwable.getMessage());
			for (StackTraceElement traceElement : throwable.getStackTrace()) {
				buffer.append("\n\tat " + traceElement);
			}
			write("[ERROR] ", buffer.toString());
		}
	}

	public static void info(String message) {
		if (INFO <= level) {
			write("[INFO ] ", message);
		}
	}

	public static void debug(String message) {
		if (DEBUG <= level) {
			write("[DEBUG] ", message);
		}
	}

	public static void debug(Throwable throwable) {
		if (DEBUG <= level) {
			StringBuffer buffer = new StringBuffer(throwable.getMessage());
			for (StackTraceElement traceElement : throwable.getStackTrace()) {
				buffer.append("\n\tat " + traceElement);
			}
			write("[DEBUG] ", buffer.toString());
		}
	}

	public static void trace(String message) {
		if (TRACE <= level) {
			write("[TRACE] ", message);
		}
	}

	private static void write(String level, String message) {
		out.print(level);
		out.println(message);
	}

}
