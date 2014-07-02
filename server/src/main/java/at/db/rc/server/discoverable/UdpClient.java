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
import static at.db.rc.Debugger.trace;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import at.db.rc.ClientHandler;
import at.db.rc.Commands;

public class UdpClient {

	private ClientHandler	eventHandler;

	public UdpClient(ClientHandler eventHandler) {
		debug("UdpClient.UdpClient()");
		this.eventHandler = eventHandler;
	}

	public ClientHandler getEventHandler() {
		return eventHandler;
	}

	public void handle(byte[] data) throws IOException {
		trace("UdpClient.handle()");
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));

		byte command = dis.readByte();
		if ((command & Commands.Command.MASK) == Commands.Command.DATA) {
			int partnerCounter = dis.readInt();
			int counter = partnerCounter + 2;
			int counterErrors = 0;
			int nextMin = (counter + 1);
			int nextMax = (counter + 10);

			if (((nextMax < nextMin) && ((partnerCounter < nextMin) && (partnerCounter > nextMax)))
					|| ((nextMax > nextMin) && ((partnerCounter < nextMin) || (partnerCounter > nextMax)))) {
				counterErrors++;
				if (counterErrors >= 10) {
					throw new IOException("too many counter errors");
				}
			} else {
				counterErrors = 0;
				counter = partnerCounter;
			}
			switch (command & Commands.Event.MASK) {
			case Commands.Event.MOUSE:
				eventHandler.handleMouseEvent(dis.readByte(), dis.readInt(), dis.readFloat(), dis.readFloat());
				break;

			case Commands.Event.KEYBOARD:
				eventHandler.handleKeyboardEvent(dis.readByte(), dis.readInt());
				break;

			case Commands.Event.MEDIA_CONTROL:
				eventHandler.handleMediaControlEvent(dis.readByte(), dis.readInt());
				break;

			case Commands.Event.SCROLL:
				eventHandler.handleScrollEvent(dis.readFloat());
				break;

			case Commands.Event.ACCELERATION:
				eventHandler.handleAccelerationEvent(dis.readFloat(), dis.readFloat(), dis.readFloat());
				break;

			case Commands.Event.TEXT:
				eventHandler.handleTextEvent(dis.readInt(), dis.readUTF());
				break;
			}
		}
	}

}
