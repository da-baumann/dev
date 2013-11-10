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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import at.db.rc.Translations;

public class QrCodeWindow {

	private JFrame	frmMain;

	public QrCodeWindow(BufferedImage image, String title, String message) {
		initialize(image, title, message);
	}

	private void initialize(BufferedImage image, String title, String message) {
		int textHeight = (!isEmpty(message)) ? 20 : 0;
		frmMain = new JFrame();
		frmMain.setTitle(title);
		frmMain.setIconImage(Toolkit.getDefaultToolkit().getImage(
				QrCodeWindow.class.getResource("/at/db/rc/remote_control.png")));
		frmMain.setResizable(false);
		frmMain.setBounds(100, 100, 400, 460 + textHeight);
		frmMain.getContentPane().setLayout(new BorderLayout(0, 0));

		if (!isEmpty(message)) {
			JLabel lblMessageArea = new JLabel(message);
			lblMessageArea.setForeground(Color.RED);
			lblMessageArea.setFont(lblMessageArea.getFont().deriveFont(Font.BOLD, 14));
			lblMessageArea.setHorizontalAlignment(SwingConstants.CENTER);
			frmMain.getContentPane().add(lblMessageArea, BorderLayout.NORTH);
		}

		ImagePanel pnlImageArea = new ImagePanel(image);
		frmMain.getContentPane().add(pnlImageArea, BorderLayout.CENTER);

		JPanel pnlButtonArea = new JPanel();
		frmMain.getContentPane().add(pnlButtonArea, BorderLayout.SOUTH);

		JButton btnClose = new JButton(Translations.BTN_CLOSE);
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeWindow();
			}
		});
		pnlButtonArea.add(btnClose);
	}

	public void closeWindow() {
		frmMain.setVisible(false);
		frmMain.dispose();
	}

	public void hildeWindow() {
		frmMain.setVisible(false);
	}

	public void showWindow() {
		frmMain.setVisible(true);
		frmMain.setAlwaysOnTop(true);
	}

	private boolean isEmpty(String message) {
		return ((message == null) || message.isEmpty());
	}

}
