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

import top.whitehat.NetUtil;

/**
 *  MAC address
 */
public class MacAddress extends PacketField {

	public static final int SIZE_IN_BYTES = 6;

	public static MacAddress NULL = new MacAddress(new byte[] { 0, 0, 0, 0, 0, 0 });

	public static MacAddress BROADCAST = new MacAddress(
			new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF });

	
	/** Convert MAC address string to Mac object  
	 * 
	 * @param strMac MAC address string, such as: "8c-ac-cf-d8-8a-e8"
	 * @return
	 */
	public static MacAddress getByName(String strMac) {
		byte[] bs = NetUtil.toBytes(strMac);
		return new MacAddress(bs);
	}
	
	public static MacAddress getByAddress(byte[] bs) {
		return new MacAddress(bs);
	}
	
	
	public MacAddress(byte[] data) {
		super(data);
		validateSize();
	}

	@Override
	public String toString() {
		return PacketUtil.toHex(data, ":");
	}

	@Override
	public int length() {
		return SIZE_IN_BYTES;
	}

	@Override
	public boolean equals(Object obj) {
		return compareTo(obj, 0) == 0;
	}

	public boolean isBroadcast() {
		return equals(BROADCAST);
	}

}
