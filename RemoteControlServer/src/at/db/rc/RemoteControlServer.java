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
import static at.db.rc.Debugger.error;
import static at.db.rc.Debugger.info;
import static at.db.rc.Debugger.trace;
import static at.db.rc.Translations.MNI_CLOSE;
import static at.db.rc.Translations.MNI_INSTALL_ANDROID_APP;
import static at.db.rc.Translations.MNI_KEYBOARD_DEUTSCH;
import static at.db.rc.Translations.MNI_KEYBOARD_ENGLISH;
import static at.db.rc.Translations.MNI_KEYBOARD_LANGUAGE;
import static at.db.rc.Translations.MNI_SHOW_SETTINGS_QR_CODE;
import static at.db.rc.Translations.MSG_DESC_REMOTE_CONTROL_SETTINGS_SCAN_SETTINGS;
import static at.db.rc.Translations.MSG_ERROR_AN_ERROR_OCCURED;
import static at.db.rc.Translations.MSG_ERROR_COULD_NOT_CREATE_CONNECTION_URL;
import static at.db.rc.Translations.MSG_ERROR_PORT_IN_USE;
import static at.db.rc.Translations.MSG_INFO_0_CONNECTIONS;
import static at.db.rc.Translations.MSG_INFO_1_CONNECTION;
import static at.db.rc.Translations.MSG_INFO_APP_STARTING_UP;
import static at.db.rc.Translations.MSG_INFO_N_CONNECTIONS;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.net.ServerSocketFactory;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import at.db.rc.FastClient.EndPointType;
import at.db.rc.keyboard.IKeyboardLayout;
import at.db.rc.keyboard.KeyboardLayoutLinuxDe;
import at.db.rc.keyboard.KeyboardLayoutLinuxEn;
import at.db.rc.keyboard.KeyboardLayoutWinDe;
import at.db.rc.keyboard.KeyboardLayoutWinEn;
import at.db.rc.server.discoverable.InteruptableSocketThread;
import at.db.rc.server.discoverable.ServerDiscovery;
import at.db.rc.server.discoverable.UdpClient;
import at.db.rc.ui.InitFrame;
import at.db.rc.ui.QrCodeWindow;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

public class RemoteControlServer implements Runnable, ITerminationListener {

	public static final String						serverVersion				= "v0.3";

	private TrayIcon											trayIcon;
	private ServerSocket									serversocket;
	private Set<FastClient>								endPoints						= new HashSet<FastClient>();
	private UdpClient											udpClient						= null;

	private int														port								= Parameters.DEFAULT_PORT;
	private int														maxConnections			= 0;
	private String												keyMd5;
	private KeyPair												keypair;
	private Integer												connections					= 0;

	private BufferedImage									originalImage;

	private QrCodeWindow									connectionUrlWindow;
	private BufferedImage									imgConnectionUrlQr;

	private QrCodeWindow									downloadUrlWindow;
	private BufferedImage									imgDownloadUrlQr;

	private InitFrame											initFrame						= null;

	private IKeyboardLayout								keyboardLayout;

	private Thread												discoveryThread			= null;

	private Map<String, CheckboxMenuItem>	keyboardCheckBoxes	= new HashMap<String, CheckboxMenuItem>();

	private Menu													mniKeyboardLanguage;

	private ServerDiscovery								serverDiscovery;

	public static void main(String[] args) {
		Debugger.init(Debugger.NONE, false);
		Translations.init();
		info("RemoteControlServer.main()");
		new Thread(new RemoteControlServer()).start();
	}

	public RemoteControlServer() {
		info("RemoteControlServer.RemoteControlServer()");
		try {
			initFrame = new InitFrame(MSG_INFO_APP_STARTING_UP);
			initFrame.showWindow();
		} catch (Exception e) {
			error(e);
		}
		try {
			tryGetPort();
			initializeUdpClient();

			generateKeyPair();
			initializeNetworking();
			startServerDiscoveryThread();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					createAndShowGUI();
					if (initFrame != null) {
						initFrame.closeWindow();
					}
					showUriQrCode();
				}
			});
		} catch (BindException e) {
			error(e);
			initFrame = new InitFrame(MSG_ERROR_PORT_IN_USE);
			initFrame.showWindow();
			debug("exiting application");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ie) {
				error(ie);
			}
			System.exit(0);
		} catch (Exception e) {
			error(e);
		}
	}

	private void tryGetPort() {
		String portProperty = System.getProperty("port");
		if (portProperty != null) {
			try {
				int portInt = Integer.parseInt(portProperty);
				if ((portInt > 0) && (portInt <= Character.MAX_VALUE)) {
					port = portInt;
				} else {
					Debugger.error("port property can not be used as port: " + portProperty);
				}
			} catch (NumberFormatException e) {
				Debugger.error("could not parse port property " + portProperty);
			}
		}
	}

	private void initializeUdpClient() {
		debug("RemoteControlServer.initializeUdpClient()");
		ClientHandler clientHandler = new ClientHandler();
		clientHandler.setKeyboardLayout(keyboardLayout);
		udpClient = new UdpClient(clientHandler);
	}

	private void startServerDiscoveryThread() {
		info("RemoteControlServer.startServerDiscoveryThread()");
		try {
			DatagramSocket socket = new DatagramSocket(port);
			serverDiscovery = new ServerDiscovery(socket, udpClient);
			discoveryThread = new InteruptableSocketThread(serverDiscovery);
			discoveryThread.start();
			debug("discoveryThread started");
		} catch (SocketException e) {
			error(e);
		}
	}

	private void createAndShowGUI() {
		info("RemoteControlServer.createAndShowGUI()");
		try {
			trayIcon = null;
			if (SystemTray.isSupported()) {
				SystemTray tray = SystemTray.getSystemTray();
				PopupMenu ppmMainMenu = new PopupMenu();

				URL imageUrl = RemoteControlServer.class.getResource("/at/db/rc/remote_control.png");
				originalImage = ImageIO.read(imageUrl);
				trayIcon = new TrayIcon(originalImage, MSG_INFO_0_CONNECTIONS, ppmMainMenu);
				BufferedImage image = updateImage(originalImage, 0);
				trayIcon.setImage(image);
				debug("trayIcon created");

				// create menu item for the default action
				MenuItem mniClose = new MenuItem(MNI_CLOSE);
				mniClose.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							debug("RemoteControlServer.createAndShowGUI().new mniClose.actionPerformed()");
							closeAllConnections();
							closeAllWindows();
							debug("exiting application");
						} catch (Exception ex) {
							error(ex);
						}
						System.exit(0);
					}
				});
				ppmMainMenu.add(mniClose);

				MenuItem mniUriQrCode = new MenuItem(MNI_SHOW_SETTINGS_QR_CODE);
				mniUriQrCode.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						showUriQrCode();
					}
				});
				ppmMainMenu.add(mniUriQrCode);

				MenuItem mniAppQrCode = new MenuItem(MNI_INSTALL_ANDROID_APP);
				mniAppQrCode.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						showAppDownloadUrl();
					}
				});
				ppmMainMenu.add(mniAppQrCode);

				mniKeyboardLanguage = new Menu(MNI_KEYBOARD_LANGUAGE);
				ppmMainMenu.add(mniKeyboardLanguage);

				CheckboxMenuItem mniKeyboardEn = new CheckboxMenuItem(MNI_KEYBOARD_ENGLISH);
				mniKeyboardEn.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						updateKeyboardLanguage("en");
					}
				});
				mniKeyboardLanguage.add(mniKeyboardEn);
				keyboardCheckBoxes.put("en", mniKeyboardEn);

				CheckboxMenuItem mniKeyboardDe = new CheckboxMenuItem(MNI_KEYBOARD_DEUTSCH);
				mniKeyboardDe.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						updateKeyboardLanguage("de");
					}
				});
				mniKeyboardLanguage.add(mniKeyboardDe);
				keyboardCheckBoxes.put("de", mniKeyboardDe);

				updateKeyboardLanguage(System.getProperty("user.language"));
				try {
					tray.add(trayIcon);
				} catch (AWTException e) {
					error(e);
				}
				debug("GUI created");

			} else {
				error("could not create tray icon ... but server should be up");
			}
		} catch (Exception e) {
			error(e);
		}
	}

	protected void updateKeyboardLanguage(String language) {
		debug("RemoteControlServer.updateKeyboardLanguage()");
		if (mniKeyboardLanguage != null) {
			for (int i = 0; i < mniKeyboardLanguage.getItemCount(); i++) {
				MenuItem item = mniKeyboardLanguage.getItem(i);
				if (item instanceof CheckboxMenuItem) {
					((CheckboxMenuItem) item).setState(false);
				}
			}
		}
		CheckboxMenuItem checkbox = keyboardCheckBoxes.get(language);
		if (checkbox != null) {
			debug("keyboardCheckBox for language [" + language + "] found");
			checkbox.setState(true);
		}
		setKeyboardLanguage(language);
	}

	private void setKeyboardLanguage(String language) {
		debug("RemoteControlServer.setKeyboardLanguage(" + language + ")");
		debug("os.name is [" + System.getProperty("os.name") + "]");
		if ("de".equals(language)) {
			if (System.getProperty("os.name").contains("Windows")) {
				keyboardLayout = new KeyboardLayoutWinDe();
			} else {
				keyboardLayout = new KeyboardLayoutLinuxDe();
			}
		} else {
			if (System.getProperty("os.name").contains("Windows")) {
				keyboardLayout = new KeyboardLayoutWinEn();
			} else {
				keyboardLayout = new KeyboardLayoutLinuxEn();
			}
		}
		for (FastClient client : endPoints) {
			IEventHandler handler = client.getEventHandler();
			if (handler instanceof ClientHandler) {
				ClientHandler clientHandler = (ClientHandler) handler;
				clientHandler.setKeyboardLayout(keyboardLayout);
			}
		}
		udpClient.getEventHandler().setKeyboardLayout(keyboardLayout);
		// info("os.name: " + System.getProperty("os.name"));
		// info("os.version: " + System.getProperty("os.version"));
		// info("user.home: " + System.getProperty("user.home"));
		// info("user.country: " +
		// System.getProperty("user.country"));
		// info("user.language: " +
		// System.getProperty("user.language"));
	}

	protected void closeAllWindows() {
		debug("RemoteControlServer.closeAllWindows()");
		if (downloadUrlWindow != null) {
			downloadUrlWindow.closeWindow();
		}
		if (connectionUrlWindow != null) {
			connectionUrlWindow.closeWindow();
		}
	}

	protected void closeAllConnections() {
		info("RemoteControlServer.closeAllConnections()");
		try {
			if (discoveryThread != null) {
				discoveryThread.interrupt();
			}
			trace("disconnectiong all clients");
			for (FastClient endPoint : new ArrayList<FastClient>(endPoints)) {
				endPoint.disconnect();
			}
			trace("closing server socket");
			serversocket.close();
			trace("joining discoveryThread");
			discoveryThread.join(5000);
		} catch (Exception e) {
			error(e);
		}
	}

	protected void showAppDownloadUrl() {
		info("RemoteControlServer.showAppDownloadUrl()");
		if (downloadUrlWindow != null) {
			debug("downloadUrlWindow already exists ... show it");
			downloadUrlWindow.showWindow();
		} else {
			debug("creating downloadUrlWindow");
			BufferedImage image = getDownloadUrlQrCode();
			if (image != null) {
				downloadUrlWindow = new QrCodeWindow(image, MNI_INSTALL_ANDROID_APP, null);
				downloadUrlWindow.showWindow();
			} else {
				JOptionPane.showMessageDialog(null, MSG_ERROR_COULD_NOT_CREATE_CONNECTION_URL, MSG_ERROR_AN_ERROR_OCCURED,
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void showUriQrCode() {
		info("RemoteControlServer.showUriQrCode()");
		if (connectionUrlWindow != null) {
			debug("connectionUrlWindow already exists ... show it");
			connectionUrlWindow.showWindow();
		} else {
			debug("creating connectionUrlWindow");
			BufferedImage image = getConnectionQrCode();
			if (image != null) {
				String message = getMessage();
				connectionUrlWindow = new QrCodeWindow(image, MSG_DESC_REMOTE_CONTROL_SETTINGS_SCAN_SETTINGS, message);
				connectionUrlWindow.showWindow();
			} else {
				JOptionPane.showMessageDialog(null, MSG_ERROR_COULD_NOT_CREATE_CONNECTION_URL, MSG_ERROR_AN_ERROR_OCCURED,
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private String getMessage() {
		try {
			debug("fetching update message");
			URL url = new URL("http://www.daniel-baumann.at/RemoteControlServer/" + serverVersion);
			URLConnection ucon = url.openConnection();
			ucon.setConnectTimeout(3000);
			InputStream inputStream = ucon.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder buffer = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				if (buffer.length() > 0) {
					buffer.append(' ');
				}
				buffer.append(line);
			}
			br.close();
			String message = buffer.toString().trim();
			debug("returning message [" + message + "]");
			return message;
		} catch (Exception e) {
			debug(e);
		}
		debug("returning message [null]");
		return null;
	}

	private void closeUriQrCode() {
		info("RemoteControlServer.closeUriQrCode()");
		if (connectionUrlWindow != null) {
			debug("hiding existing connectionUrlWindow");
			connectionUrlWindow.hildeWindow();
		}
	}

	@Override
	public void onTerminated(FastClient endPoint) {
		info("RemoteControlServer.onTerminated()");
		serverDiscovery.removeAddress(endPoint.getSocket().getInetAddress().getAddress());
		endPoints.remove(endPoint);
		synchronized (connections) {
			connections--;
			if (connections < 0) {
				connections = 0;
			}
			if (connections == 0) {
				showUriQrCode();
			}
		}
		updateTrayMenu(connections);
	}

	private void updateTrayMenu(int connections) {
		info("RemoteControlServer.updateTrayMenu()");
		if (trayIcon != null) {
			trayIcon.setToolTip((connections != 1) ? String.format(MSG_INFO_N_CONNECTIONS, connections)
					: MSG_INFO_1_CONNECTION);
			if (originalImage != null) {
				BufferedImage image = updateImage(originalImage, connections);
				trayIcon.setImage(image);
			}
		}
	}

	private BufferedImage updateImage(BufferedImage image, int connections) {
		info("RemoteControlServer.updateImage()");
		int w = trayIcon.getSize().width - 3;
		int h = trayIcon.getSize().height - 3;
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.drawImage(image, 0, 0, w, h, null);
		g2d.setPaint(Color.white);
		g2d.setFont(new Font("Arial", Font.BOLD, 10));
		String text = "" + connections;
		int x = 8;
		int y = 15;
		g2d.drawString(text, x, y);
		g2d.setPaint(Color.black);
		x = 9;
		y = 16;
		g2d.drawString(text, x, y);
		g2d.dispose();
		return img;
	}

	private BufferedImage getConnectionQrCode() {
		info("RemoteControlServer.getConnectionQrCode()");
		if (imgConnectionUrlQr == null) {
			imgConnectionUrlQr = getQrCode(getConnectionUrl());
		}
		return imgConnectionUrlQr;
	}

	private BufferedImage getDownloadUrlQrCode() {
		info("RemoteControlServer.getDownloadUrlQrCode()");
		if (imgDownloadUrlQr == null) {
			imgDownloadUrlQr = getQrCode("market://search?q=pname:at.db.rc.free");
		}
		return imgDownloadUrlQr;
	}

	private BufferedImage getQrCode(String connectionUrl) {
		debug("creating QR code image for [" + connectionUrl + "]");
		BufferedImage qrImage = null;
		try {
			if (connectionUrl != null) {
				QRCode qrCode = Encoder.encode(connectionUrl, ErrorCorrectionLevel.L);
				ByteMatrix matrix = qrCode.getMatrix();

				// generate an imgConnectionUrlQr from the byte matrix
				int width = matrix.getWidth();
				int height = matrix.getHeight();

				byte[][] array = matrix.getArray();

				int pixelSize = 10;
				// create buffered imgConnectionUrlQr to draw to
				qrImage = new BufferedImage(width * pixelSize, height * pixelSize, BufferedImage.TYPE_INT_RGB);

				// iterate through the matrix and draw the pixels to the
				// imgConnectionUrlQr
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int grayValue = array[y][x] & 0xff;
						for (int i = 0; i < pixelSize; i++) {
							for (int j = 0; j < pixelSize; j++) {
								qrImage.setRGB(x * pixelSize + i, y * pixelSize + j, (grayValue == 1 ? 0 : 0xFFFFFF));
							}
						}
					}
				}
			} else {
				qrImage = null;
			}
		} catch (Exception e) {
			error(e);
		}
		return qrImage;
	}

	private String getConnectionUrl() {
		info("RemoteControlServer.getConnectionUrl()");
		try {
			String hostName = null;
			String hostAddress = null;
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			boolean found = false;
			debug("* checking network interfaces:");
			while (interfaces.hasMoreElements()) {
				NetworkInterface current = interfaces.nextElement();
				debug("** current: " + current.getDisplayName() + " | " + current.getName());
				if (current.isUp() && !current.isLoopback() && !current.isVirtual() && !isVmWareInterface(current)) {
					Enumeration<InetAddress> addresses = current.getInetAddresses();
					while (!found && addresses.hasMoreElements()) {
						InetAddress address = addresses.nextElement();
						debug("*** adderss: " + address);
						if (!address.isLoopbackAddress() && (address instanceof Inet4Address)) {
							debug("**** set as found");
							found = true;
							hostName = address.getHostName();
							hostAddress = address.getHostAddress();
						}
					}
				}
			}
			String connectionUrl = "http://remotehost/" + ((hostAddress == null) ? hostName : hostAddress) + "/" + port + "/"
					+ keyMd5;
			debug("connection url: " + connectionUrl);
			return connectionUrl;
		} catch (Exception e) {
			error(e);
		}
		debug("connection url: null");
		return null;
	}

	private boolean isVmWareInterface(NetworkInterface networkInterface) {
		return networkInterface.getName().toLowerCase().contains("vmnet");
	}

	private void generateKeyPair() {
		info("RemoteControlServer.generateKeyPair()");
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance(Parameters.KEY_ALGORITHM);
			SecureRandom sr = new SecureRandom();

			keyGen.initialize(Parameters.RSA_KEY_LENGTH, sr);
			keypair = keyGen.generateKeyPair();
			keyMd5 = Conv.toHex(Conv.toMD5(keypair.getPublic().getEncoded()));
			debug("md5sum = " + keyMd5);
		} catch (Exception e) {
			error(e);
		}
	}

	@Override
	public void run() {
		info("RemoteControlServer.run()");
		try {
			while ((connections + 1 < maxConnections) || (maxConnections == 0)) {
				Socket socket = null;
				try {
					debug("RemoteControlServer.run() ... accepting connection");
					socket = serversocket.accept();
				} catch (Exception e) {
					error(e);
					return;
				}
				closeUriQrCode();
				debug("creating endpoint");
				serverDiscovery.addAddress(socket.getInetAddress().getAddress());
				FastClient endpoint = new FastClient(socket, EndPointType.SERVER);
				endpoint.setKeypair(keypair);
				ClientHandler clientHandler = new ClientHandler();
				clientHandler.setKeyboardLayout(keyboardLayout);
				endpoint.setEventHandler(clientHandler);
				endpoint.setTerminationListener(this);
				endPoints.add(endpoint);
				Thread thread = new Thread(endpoint);
				thread.start();
				synchronized (connections) {
					connections++;
				}
				updateTrayMenu(connections);
			}
		} catch (InterruptedException e) {
			error(e);
		} catch (ExecutionException e) {
			if (e.getCause() != null) {
				error(e.getCause());
			} else {
				error(e);
			}
		}
	}

	private void initializeNetworking() throws IOException {
		info("RemoteControlServer.initializeNetworking()");
		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		serversocket = factory.createServerSocket(port);
		debug("Server running at [" + serversocket.getInetAddress() + " : " + serversocket.getLocalPort() + "].");
	}

}
