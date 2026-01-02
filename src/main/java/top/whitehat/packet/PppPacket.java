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

/**
 * PPP Packet
 <pre>

PPP packet format
 
+----------+----------+----------+-------------+----------------+----------+
|  Flag    | Address  | Control  | Protocol    | Information    |  CRC     |
|  8 bit   | 8 bit    | 8 bit    |  16 bit     | (Payload)      |  16 bit  |
+----------+----------+----------+-------------+----------------+----------+

Flag           : byte  0,  1 bytes  , value=0x7E, Marks the beginning and end of a PPP frame.
Address        : byte  1,  1 bytes  , value=Always 0xFF(broadcast address).
Control        : byte  2,  1 bytes  , value=Always 0x03.Indicates an unnumbered information (UI) frame, for compatibility with HDLC.
Protocol       : byte  3,  2 bytes  , value=IPv4 (0x0021), IPv6 (0x0057), LCP (0xC021), PAP (0xC023), CHAP (0xC223), etc.Identifies the type of ​​payload protocol​​ being carried.
CRC            : byte  ?,  2 bytes  , optional, CRC-16
 </pre>
 */
public class PppPacket extends Layer2Packet {
	
	/* offset of each fields */
	private static int OFFSET_FLAG = 0; // 8 bit
	private static int OFFSET_ADDR = 1; // 8 bit
	private static int OFFSET_CTRL = 2; // 8 bit
	private static int OFFSET_PROTOCOL = 3; // 16 bit
	
	/** flag value for PPP packet */
	public static int FLAG = 0x7E;
	
	private static int FIXED_LENGTH = 5;
	
	/* PPP protocols */
	protected static int PPP_IPV4 = 0x0021;
	protected static int PPP_IPV6 = 0x0057;
	protected static int PPP_LCP = 0xC021;
	protected static int PPP_PAP = 0xC023;
	protected static int PPP_CHAP = 0xC223;
	
	/** whether the array is PPP packet data */
	public static boolean detect(byte[] array) {
		if(array[OFFSET_FLAG] == FLAG && array[OFFSET_ADDR] == 0xFF) {
			return true;
		}
		return false;
	}
	
	public PppPacket() {
		super();
	}
	
	public PppPacket(byte[] data) {
		super(data);
	}
	
	/** Returns the header length in bytes. */
	public int headerLength() {
		return FIXED_LENGTH;
	}
	
	/** Returns the packet length in bytes. */
	public Packet headerLength(int len) {
		if (len != FIXED_LENGTH) throw new PacketException("PppPacket packet header length should be 14");
		return this;
	}
	
	private int toIPV4V6(int t) {
		if (t == PPP_IPV4) t = Ethernet.IPV4;
		else if (t == PPP_IPV6) t = Ethernet.IPV6;
		return t;
	}
	
	private int toPPP_V4V6(int t) {
		if (t == Ethernet.IPV4) t = PPP_IPV4;
		else if (t == Ethernet.IPV6) t = PPP_IPV6;
		return t;
	}
	
	/** get type */
	public int type() {
		int t = getUInt16(OFFSET_PROTOCOL);
		return toIPV4V6(t);
	}

	/** set type */
	public PppPacket type(int value) {
		int t = toPPP_V4V6(value);
		putUInt16(OFFSET_PROTOCOL, t);
		return this;
	} 
	
	/** get flag */
	public int flag() {
		return getUInt8(OFFSET_FLAG);
	}

	/** set flag */
	public PppPacket flag(int value) {
		putUInt8(OFFSET_FLAG, value);
		return this;
	}
	
	/** get address */
	public int address() {
		return getUInt8(OFFSET_ADDR);
	}

	/** set address */
	public PppPacket address(int value) {
		putUInt8(OFFSET_ADDR, value);
		return this;
	}
	
	/** get control */
	public int control() {
		return getUInt8(OFFSET_CTRL);
	}

	/** set control */
	public PppPacket control(int value) {
		putUInt8(OFFSET_CTRL, value);
		return this;
	}
	
	/** get protocol */
	public int protocol() {
		return getUInt16(OFFSET_PROTOCOL);
	}

	/** set protocol */
	public PppPacket protocol(int value) {
		value = toPPP_V4V6(value);
		putUInt16(OFFSET_PROTOCOL, value);
		return this;
	}

	@Override
	public MacAddress srcMac() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Layer2Packet srcMac(MacAddress addr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MacAddress dstMac() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Layer2Packet dstMac(MacAddress addr) {
		// TODO Auto-generated method stub
		return null;
	}
	

	
	
}
