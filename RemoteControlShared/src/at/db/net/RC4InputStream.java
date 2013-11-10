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
package at.db.net;

import java.io.IOException;
import java.io.InputStream;

public class RC4InputStream extends InputStream {

	/*
	 * private byte[] decrypt(byte[] plaintext) { final byte[] ciphertext = new
	 * byte[plaintext.length]; int i = 0, j = 0, k, t; for (int counter = 0;
	 * counter < plaintext.length; counter++) { i = (i + 1) & 0xFF; j = (j + S[i])
	 * & 0xFF; S[i] ^= S[j]; S[j] ^= S[i]; S[i] ^= S[j]; t = (S[i] + S[j]) & 0xFF;
	 * k = S[t]; ciphertext[counter] = (byte) (plaintext[counter] ^ k); } return
	 * ciphertext; }
	 */

	private final byte[]	S	= new byte[256];
	private final byte[]	T	= new byte[256];
	private int						i	= 0;
	private int						j	= 0;

	private final int			keylen;
	private InputStream		in;

	public RC4InputStream(InputStream in, byte[] key) {
		this.in = in;

		if (key.length < 5 || key.length > 256) {
			throw new IllegalArgumentException("key must be between 5 and 256 bytes");
		} else {
			keylen = key.length;
			for (int i = 0; i < 256; i++) {
				S[i] = (byte) i;
				T[i] = key[i % keylen];
			}
			int j = 0;
			for (int i = 0; i < 256; i++) {
				j = (j + S[i] + T[i]) & 0xFF;
				S[i] ^= S[j];
				S[j] ^= S[i];
				S[i] ^= S[j];
			}
		}
	}

	public int decrypt(byte plain) {
		i = (i + 1) & 0xFF;
		j = (j + S[i]) & 0xFF;
		S[i] ^= S[j];
		S[j] ^= S[i];
		S[i] ^= S[j];
		int t = (S[i] + S[j]) & 0xFF;
		int k = S[t];
		byte cipher = (byte) (plain ^ k);
		return cipher;
	}

	@Override
	public int read() throws IOException {
		return decrypt((byte) (in.read())) & 0xFF;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

}
