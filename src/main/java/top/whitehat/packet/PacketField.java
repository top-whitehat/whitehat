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
package top.whitehat.packet;

/** A field in a packet that holds several bytes of data.​ */
public class PacketField {

	/** field data */
	protected final byte[] data;
	
	public PacketField(byte[] data) {
		this.data = data;
	}
	
	public PacketField(int length) {
		this(new byte[length]);
	}
	
	/** validate data size */
	protected void validateSize() {
		if (data == null) throw new NullPointerException();
		if (data.length != length()) 
			throw new ArrayIndexOutOfBoundsException("data size expect " + length() + " but " + data.length + " is found");
	}
	
	/** get the data bytes */
	public byte[] array() {
		return data;
	}

	/** length of data bytes */
	public int length() {
		return data == null ? 0 : data.length;
	}
	
	/** read from packet at specified offset */
	public PacketField read(Packet pkt, int offset) {
		pkt.getBytes(offset, length());
		return this;
	}
	
	/** write to packet at specified offset */
	public PacketField write(Packet pkt, int offset) {
		pkt.putBytes(offset, data);
		return this;
	}
	
	/** compare to other byte object */
	protected int compareTo(Object obj, int offset) {
		byte[] buf = null;
		
		if (obj instanceof PacketField)
			buf = ((PacketField) obj).data;
		else if (obj instanceof byte[])
			buf = (byte[]) obj;
		else
			return 99;

		if (data == null) return -1;
		if (buf == null) return 1;
		
		int len = buf.length;
		if (offset < 0) offset = len + offset;
		
		for (int i = 0; i < data.length; i++) {
			if (offset+i >= len) return 1;
			if (data[i] != buf[i])
				return data[i] > buf[i] ? 1 : -1;
		}
		
		return 0;
	}
}
