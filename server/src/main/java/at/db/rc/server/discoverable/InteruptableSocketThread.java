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
package at.db.rc.server.discoverable;

import static at.db.rc.Debugger.debug;
import static at.db.rc.Debugger.error;
import static at.db.rc.Debugger.trace;

import java.net.DatagramSocket;

public class InteruptableSocketThread extends Thread {

	private final ISocketRunnable<DatagramSocket>	runnable;

	public InteruptableSocketThread(ISocketRunnable<DatagramSocket> runnable) {
		super(runnable);
		debug("InteruptableSocketThread.InteruptableSocketThread()");
		this.runnable = runnable;
	}

	@Override
	public void interrupt() {
		debug("InteruptableSocketThread.interrupt()");
		super.interrupt();
		if (runnable != null) {
			final DatagramSocket socket = runnable.getSocket();
			if (socket != null) {
				Thread closeThread = new Thread(new Runnable() {
					@Override
					public void run() {
						trace("closing DatagramSocket socket");
						socket.close();
						trace("DatagramSocket socket closed");
					}
				});
				closeThread.start();
				try {
					closeThread.join(1000);
				} catch (InterruptedException e) {
					error(e);
				}
			}
		}
	}

}
