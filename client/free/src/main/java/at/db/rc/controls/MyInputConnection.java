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
package at.db.rc.controls;

import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import at.db.rc.interfaces.ITextListener;

class MyInputConnection extends BaseInputConnection {

	private ITextListener	textListener;

	public MyInputConnection(View targetView, boolean fullEditor, ITextListener textListener) {
		super(targetView, fullEditor);
		this.textListener = textListener;
	}

	public boolean commitText(CharSequence text, int newCursorPosition) {
		textListener.updateText(text);
		return true;
	}

}