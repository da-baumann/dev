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
package at.db.rc.keyboard;

import java.awt.event.KeyEvent;

public class KeyboardLayoutWinDe implements IKeyboardLayout {

	@Override
	public int[] getKeyCodes(char character) {
		switch (character) {

		// Lower case characters
		case 'a':
			return new int[] { KeyEvent.VK_A };
		case 'b':
			return new int[] { KeyEvent.VK_B };
		case 'c':
			return new int[] { KeyEvent.VK_C };
		case 'd':
			return new int[] { KeyEvent.VK_D };
		case 'e':
			return new int[] { KeyEvent.VK_E };
		case 'f':
			return new int[] { KeyEvent.VK_F };
		case 'g':
			return new int[] { KeyEvent.VK_G };
		case 'h':
			return new int[] { KeyEvent.VK_H };
		case 'i':
			return new int[] { KeyEvent.VK_I };
		case 'j':
			return new int[] { KeyEvent.VK_J };
		case 'k':
			return new int[] { KeyEvent.VK_K };
		case 'l':
			return new int[] { KeyEvent.VK_L };
		case 'm':
			return new int[] { KeyEvent.VK_M };
		case 'n':
			return new int[] { KeyEvent.VK_N };
		case 'o':
			return new int[] { KeyEvent.VK_O };
		case 'p':
			return new int[] { KeyEvent.VK_P };
		case 'q':
			return new int[] { KeyEvent.VK_Q };
		case 'r':
			return new int[] { KeyEvent.VK_R };
		case 's':
			return new int[] { KeyEvent.VK_S };
		case 't':
			return new int[] { KeyEvent.VK_T };
		case 'u':
			return new int[] { KeyEvent.VK_U };
		case 'v':
			return new int[] { KeyEvent.VK_V };
		case 'w':
			return new int[] { KeyEvent.VK_W };
		case 'x':
			return new int[] { KeyEvent.VK_X };
		case 'y':
			return new int[] { KeyEvent.VK_Y };
		case 'z':
			return new int[] { KeyEvent.VK_Z };

			// Upper case characters
		case 'A':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_A };
		case 'B':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_B };
		case 'C':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_C };
		case 'D':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_D };
		case 'E':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_E };
		case 'F':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_F };
		case 'G':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_G };
		case 'H':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_H };
		case 'I':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_I };
		case 'J':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_J };
		case 'K':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_K };
		case 'L':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_L };
		case 'M':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_M };
		case 'N':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_N };
		case 'O':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_O };
		case 'P':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_P };
		case 'Q':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_Q };
		case 'R':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_R };
		case 'S':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_S };
		case 'T':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_T };
		case 'U':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_U };
		case 'V':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_V };
		case 'W':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_W };
		case 'X':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_X };
		case 'Y':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_Y };
		case 'Z':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_Z };

			// Space characters symbols
		case ' ':
			return new int[] { KeyEvent.VK_SPACE };
		case '\t':
			return new int[] { KeyEvent.VK_TAB };
		case '\n':
			return new int[] { KeyEvent.VK_ENTER };
		case '\r':
			return new int[] { KeyEvent.VK_ENTER };

			// Punctuation symbols
		case ',':
			return new int[] { KeyEvent.VK_COMMA };
		case '.':
			return new int[] { KeyEvent.VK_PERIOD };
		case '?':
			return new int[] { KeyEvent.VK_SHIFT, 91 };
		case '!':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_1 };
		case ';':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA };
		case ':':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD };
		case '\'':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_NUMBER_SIGN };
		case '`':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_DEAD_ACUTE };
		case '´':
			return new int[] { KeyEvent.VK_DEAD_ACUTE };
		case '"':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_2 };

			// Brackets
		case '(':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_8 };
		case ')':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_9 };
		case '[': /* TODO */
			return new int[] {};
		case ']': /* TODO */
			return new int[] {};
		case '{': /* TODO */
			return new int[] {};
		case '}': /* TODO */
			return new int[] { /* TODO */};

			// German special characters
		case 'ä':
			return new int[] { 222 };
		case 'ö':
			return new int[] { 192 };
		case 'ü':
			return new int[] { 59 };
		case 'Ä':
			return new int[] { KeyEvent.VK_SHIFT, 222 };
		case 'Ö':
			return new int[] { KeyEvent.VK_SHIFT, 192 };
		case 'Ü':
			return new int[] { KeyEvent.VK_SHIFT, 59 };
		case 'ß':
			return new int[] { 91 };

			// Numbers 0-9
		case '0':
			return new int[] { KeyEvent.VK_0 };
		case '1':
			return new int[] { KeyEvent.VK_1 };
		case '2':
			return new int[] { KeyEvent.VK_2 };
		case '3':
			return new int[] { KeyEvent.VK_3 };
		case '4':
			return new int[] { KeyEvent.VK_4 };
		case '5':
			return new int[] { KeyEvent.VK_5 };
		case '6':
			return new int[] { KeyEvent.VK_6 };
		case '7':
			return new int[] { KeyEvent.VK_7 };
		case '8':
			return new int[] { KeyEvent.VK_8 };
		case '9':
			return new int[] { KeyEvent.VK_9 };

			// Mathematical operators
		case '=':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_0 };
		case '*':
			return new int[] { KeyEvent.VK_MULTIPLY };
		case '+':
			// return new int[] { KeyEvent.VK_PLUS };
			return new int[] { KeyEvent.VK_ADD };
		case '-':
			// return new int[] { KeyEvent.VK_MINUS };
			return new int[] { KeyEvent.VK_SUBTRACT };
		case '/':
			return new int[] { KeyEvent.VK_DIVIDE };
		case '<':
			return new int[] { KeyEvent.VK_LESS };
		case '>':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_LESS };
		case '#':
			return new int[] { KeyEvent.VK_NUMBER_SIGN };
		case '%':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_5 };
		case '$':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_4 };
		case '€': /* TODO */
			return new int[] {};

			// General typography
		case '@': /* TODO */
			return new int[] {};
		case '~': /* TODO */
			return new int[] {};
		case '^':
			return new int[] { KeyEvent.VK_DEAD_CIRCUMFLEX };
		case '°':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_DEAD_CIRCUMFLEX };
		case '§':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_3 };
		case '&':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_6 };
		case '_':
			return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_MINUS };
		case '\\': /* TODO */
			return new int[] {};
		case '|': /* TODO */
			return new int[] {};
		}

		return new int[0];
	}
}
