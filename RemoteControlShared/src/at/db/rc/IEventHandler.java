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

public interface IEventHandler {

  void handleMouseEvent(byte action, int buttonMask, float x, float y);

  void handleKeyboardEvent(byte action, int keyCode);

  void handleMediaControlEvent(byte action, int keyCode);

  void handleScrollEvent(float y);

  void handleAccelerationEvent(float x, float y, float z);

  void handleTextEvent(int action, String text);

}
