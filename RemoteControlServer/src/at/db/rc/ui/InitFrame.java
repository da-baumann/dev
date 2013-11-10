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
package at.db.rc.ui;

import static at.db.rc.Debugger.debug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class InitFrame {

	private JFrame	frmMain;

	public InitFrame(String message) {
		debug("InitFrame(String message)");
		initialize(message);
	}

	private void initialize(String message) {
		debug("void InitFrame.initialize(String message)");
		frmMain = new JFrame();
		frmMain.setResizable(false);
		frmMain.setUndecorated(true);
		frmMain.setIconImage(Toolkit.getDefaultToolkit().getImage(
				QrCodeWindow.class.getResource("/at/db/rc/remote_control.png")));
		frmMain.setTitle(message);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frmMain.setBounds((screenSize.width - 300) / 2, (screenSize.height - 50) / 2, 300, 50);
		frmMain.getContentPane().setLayout(new BorderLayout(0, 0));

		JLabel lblMessageArea = new JLabel(message);
		lblMessageArea.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		lblMessageArea.setForeground(Color.BLUE);
		lblMessageArea.setFont(lblMessageArea.getFont().deriveFont(Font.BOLD, 14));
		lblMessageArea.setHorizontalAlignment(SwingConstants.CENTER);
		frmMain.getContentPane().add(lblMessageArea, BorderLayout.CENTER);
	}

	public void closeWindow() {
		debug("void InitFrame.closeWindow()");
		frmMain.setVisible(false);
		frmMain.dispose();
	}

	public void showWindow() {
		debug("void InitFrame.closeWindow()");
		frmMain.setVisible(true);
		frmMain.setAlwaysOnTop(true);
	}

}
