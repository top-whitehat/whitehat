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

import top.whitehat.util.ByteArray;

/**
 * A BasePacket is a packet that consists of a header and a payload.​
 */
public class BasePacket extends ByteArray {
	
	/** header size in bytes */
	public static int HEADER_SIZE = 0;

	/** constructor */
	public BasePacket() {
		super();
	}
	
	/** constructor */
	public BasePacket(byte[] buf) {
		super(buf);
	}
	
	/** Returns the header length in bytes. */
	public int headerLength() {
		return HEADER_SIZE;
	}

	/** Returns the packet length in bytes. */
	public BasePacket headerLength(int len) {
		HEADER_SIZE = len;
		return this;
	}
	
	/** get payload length */
	public int payloadLength() {
		return -1;
	}

	/** get header */
	public byte[] header() {
		return getBytes(0, headerLength());
	}
	
	/** set header */
	public BasePacket header(byte[] data) {
		putBytes(0, data);
		return this;
	}
	
	/** get payload */
	public byte[] payload() {
		return getBytes(headerLength(), payloadLength());
	}
	
	/** set set payload */
	public BasePacket payload(byte[] data) {
		put(headerLength(), data);
		return this;
	}

	/** get class name */
	protected String getClassName() {
		String ret = this.getClass().getSimpleName();
		if (ret.endsWith("Packet") && ret != "Packet")
			ret = ret.substring(0, ret.length() - 6);
		return ret;
	}
	
	
}
