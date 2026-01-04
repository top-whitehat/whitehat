/*
 * Copyright 2026 The WhiteHat Project
 *
 * The WhiteHat Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package top.whitehat.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/** Using ByteArray object as an OutputStream */
public class ByteArrayOutputStream extends OutputStream {

	ByteArray array;
	
	public ByteArrayOutputStream(ByteArray array) {
		this.array = array;
	}
	
	public ByteArrayOutputStream(int size) {
		this.array = new ByteArray(size);
	}

	@Override
	public void write(int b) throws IOException {
		array.put((byte)(b & 0xFF));
	}
	
	public int size() {
		return array.limit();
	}
	
	public String toString(String charset) {
		try {
			return new String(array.getBytes(), charset);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public byte[] toByteArray() {
		return array.getBytes();
	}
}
