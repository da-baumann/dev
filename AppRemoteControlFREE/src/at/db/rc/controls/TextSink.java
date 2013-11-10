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

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import at.db.rc.interfaces.ITextListener;

public class TextSink extends EditText {

	public static final String	TAG	= TextSink.class.getName();
	private ITextListener				textListener;
	private MyInputConnection		inputConnection;

	public ITextListener getTextListener() {
		return textListener;
	}

	public void setTextListener(ITextListener textListener) {
		this.textListener = textListener;
		setOnKeyListener(textListener);
	}

	public TextSink(Context context) {
		super(context);
		configure();
	}

	public TextSink(Context context, AttributeSet attrs) {
		super(context, attrs);
		configure();
	}

	public TextSink(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		configure();
	}

	private void configure() {
		setFocusableInTouchMode(true);
	}

	@Override
	public boolean onCheckIsTextEditor() {
		return true;
	}

	@Override
	public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
		editorInfo.actionLabel = null;
		editorInfo.label = "Test text";
		editorInfo.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
		editorInfo.imeOptions = EditorInfo.IME_ACTION_NONE;

		return getInputConnection();
	}

	private InputConnection getInputConnection() {
		if (inputConnection == null) {
			inputConnection = new MyInputConnection(this, true, textListener);
		}
		return inputConnection;
	}

}
