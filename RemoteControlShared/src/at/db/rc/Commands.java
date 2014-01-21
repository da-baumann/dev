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

public class Commands {

  /*

  Structure:
    Type: 2 bit

  00...... Command.CONTROL
  10...... Command.DATA
  01...... Command.STATUS
  ....0000 Type: CONTROL | EVENT | STATUS

  0000.... 0
  0001.... 1
  0010.... 2
  0011.... 3
  0100.... 4
  0101.... 5
  0110.... 6
  0111.... 7
  1000.... 8
  1001.... 9
  1010.... a
  1011.... b
  1100.... c
  1101.... d
  1110.... e
  1111.... f

   */


	public static final class None {
		public static final byte	Mask	= 0;	// (byte) 0b0000_0000
	}

	public static final class Command {
		public static final byte	MASK		= -64;	// (byte) 0b1100_0000; // 0xc.
		public static final byte	CONTROL	= 0;		// (byte) 0b0000_0000; // 0x0.
		public static final byte	DATA		= -128; // (byte) 0b1000_0000; // 0x8.
		public static final byte	STATUS	= 64;	// (byte) 0b0100_0000; // 0x4.
	}

	public static final class Control {
		public static final byte	MASK										= 7;		// (byte)
		public static final byte	HANDSHAKE_START					= 0x00; // (byte)
		public static final byte	HANDSHAKE_PUBLIC_KEY		= 0x01; // (byte)
		public static final byte	CHECK_PROTOCOL_VERSION	= 0x02; // (byte)
		public static final byte	CONNECT									= 0x03; // (byte)
		public static final byte	DISCONNECT							= 0x04; // (byte)
	}

	public static final class Event {
		public static final byte	MASK					= 7;		// (byte) 0b0000_0111; //
		public static final byte	MOUSE					= 0x00; // (byte) 0b0000_0000; //
		public static final byte	KEYBOARD			= 0x01; // (byte) 0b0000_0001; //
		public static final byte	MEDIA_CONTROL	= 0x02; // (byte) 0b0000_0010; //
		public static final byte	SCROLL				= 0x03; // (byte) 0b0000_0011; //
		public static final byte	ACCELERATION	= 0x04; // (byte) 0b0000_0100; //
		public static final byte	TEXT					= 0x05; // (byte) 0b0000_0101; //
	}

	public static final class Status {
		public static final byte	MASK						= 7;		// (byte) 0b0000_0111; //
		public static final byte	SUCCESS					= 0x00; // (byte) 0b0000_0000; //
		public static final byte	ERROR						= 0x01; // (byte) 0b0000_0001; //
		public static final byte	WRONG_PASSWORD	= 0x02; // (byte) 0b0000_0010; //
	}

}
