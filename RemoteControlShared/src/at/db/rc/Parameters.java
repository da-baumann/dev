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

public class Parameters {

	public static final String	KEY_ALGORITHM					= "RSA";
	public static final String	CIPHER_TRANSFORMATION	= "RSA/ECB/PKCS1Padding";
	public static final int			RSA_KEY_LENGTH				= 512;

	public static final int			DEFAULT_PORT					= 12121;

	public static final byte		DISCOVERY_REQUEST			= 1;
	public static final byte		DISCOVERY_RESPONSE		= 2;

}
