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

/** Tag-Length-Value object */
public class TLV implements IReadWriteByteArray {
	
	public int tag;
	
	public byte[] value;
	
	public TLV() {
		
	}
	
	public TLV(int tag, byte[] value) {
		this.tag = tag;
		this.value = value;
	}
	
	public int length() {
		return value == null ? 0 : value.length;
	}

	@Override
	public int readByteArray(ByteArray b, int bitOffset) {
		tag = b.getUInt8();
		int len = b.getUInt8();
		value = b.getBytes(len);
		return 0;
	}

	@Override
	public int writeByteArray(ByteArray b, int bitOffset) {
		b.putUInt8(tag);
		b.putUInt8(length());
		b.putBytes(value);
		return 0;
	}
}
