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
import java.io.InputStream;

/** Using ByteArray object as an InputStream */
public class ByteArrayInputStream extends InputStream {

	ByteArray array;
	
	public ByteArrayInputStream(ByteArray array) {
		this.array = array;
	}

	@Override
	public int read() throws IOException {
		if (array.readerIndex() < array.limit())
			return array.getByte() & 0xFF;
		else
			return -1;
	}
	
	@Override
	public int available() throws IOException {
		 return array.limit() - array.readerIndex();
	}
	 
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
//		Objects.checkFromIndexSize(off, len, b.length);
		if (len == 0) {
            return 0;
        }
		
		int avaliable = available();
		int copyLength = avaliable > len ? len : avaliable;
		
		array.get(b, off, copyLength);
		return copyLength == 0 ? -1 : copyLength;
	}
	
}
