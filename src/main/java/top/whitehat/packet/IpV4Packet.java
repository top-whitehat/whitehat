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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import top.whitehat.util.ByteArray;

/**
 * IPv4 Packet <br>
 * 
 * <a href="https://www.rfc-editor.org/rfc/rfc791">RFC791</a>
 *
 * <pre>
 
 IPv4 packet format 
 
 0               1               2               3  
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|Version|  IHL  |      TOS       |         Total Length         |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|         Identification         |Flags|    Fragment Offset     |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|    TTL        |    Protocol    |       Header Checksum        |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                       Source IP Address                       |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                      Destination IP Address                   |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                       Options (variable length)               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                        Padding (if needed)                    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                    Payload (TCP/UDP/ICMP)                     |
+---------------------------------------------------------------+

Version        : byte  0,  4 bits(high) , value=4 (IPV4)
IHL            : byte  0,  4 bits(lower), value=5 (means header length 5*4=20 bytes)
TOS            : byte  1,  8 bits       , value=0 (Type of Service)
Total Length   : byte  2, 16 bits       , value=20 ~ 65535 (length of whole packet)
Identification : byte  4, 16 bits       , value=random (used in fragment)
Flags          : byte  6,  4 bits(high) , value=random (used in fragment)
Fragment Offset: byte  6, 12 bits(lower), value=random (used in fragment)
TTL            : byte  8,  8 bits       , value=0 ~ 255   (each router decrease one)
Protocol       : byte  9,  8 bits(high) , value=6 (TCP) ,  17 (UDP) , 11 (ICMP)
Checksum       : byte 10, 16 bits       , value=CRC   (Header Checksum)
Source IP      : byte 12, 32 bits       , value=4 bytes (Source IPv4 address) 
Destination IP : byte 16, 32 bits       , value=4 bytes (Destination IPv4 address)
Options        : byte 20, variable length (Options data)
 * </pre>
 */
public class IpV4Packet extends IpPacket {

	// field offset
	private static int OFFSET_HEADER_LENGTH = 0; // 4 bits
	private static int OFFSET_TOS = 1; // 8 bits
	protected static int OFFSET_TOTAL_LENGTH = 2; // 16 bits
	protected static int OFFSET_ID = 4; // 16 bits
	private static int OFFSET_FLAGS = 5; // 4 bits
	private static int OFFSET_FRAGMENT = 5; // 12 bits
	private static int OFFSET_TTL = 8; // 8 bits
	private static int OFFSET_PROTOCOL = 9; // 8 bits
	private static int OFFSET_CHECKSUM = 10;// 16 bits
	public static int OFFSET_SRC_IPV4 = 12; // 32 bits
	private static int OFFSET_DST_IPV4 = 16; // 32 bits
	private static int OFFSET_OPTIONS = 20;
	public static int HEADER_SIZE = 20;

	/** Create a IPv4Packet with specified protocol, srcIp, dstIp, specified payload
	 * data size
	 */
	protected static IpV4Packet createNew(int protocol, InetAddress srcIp, InetAddress dstIp, int dataSize) {
		int totalSize = IpV4Packet.HEADER_SIZE + dataSize;
		IpV4Packet p = new IpV4Packet();
		p.init(totalSize);
		p.version(4).ttl(0xFF).protocol(protocol);
		p.totalLength(totalSize);
		if (srcIp != null) p.srcIp(srcIp);		
		if (dstIp != null) p.dstIp(dstIp);				
		if (dataSize == 0) p.checksum(0);  // update checksum only when data is empty
		return p;
	}
	
	/** Wrap a byte array from a specified offset and length, return an IpV4Packet. */
	public static IpPacket wrap(byte[] data, int offset, int length) {
		IpV4Packet p = new IpV4Packet();
		p.array(data, offset, length);
		return p;
	}

	/** constructor */
	public IpV4Packet() {
		super();
	}

	/** constructor */
	public IpV4Packet(byte[] buf) {
		super(buf);
	}

	@Override
	public int headerLength() {
		int n = getUInt4(OFFSET_HEADER_LENGTH, ByteArray.LOW_BITS);
		return n * 4;
	}

	@Override
	public IpV4Packet headerLength(int value) {
		int m = value % 4;
		if (m != 0) {
			value += (4 - m);
		}
		putUInt4(OFFSET_HEADER_LENGTH, ByteArray.LOW_BITS, value / 4);
		return this;
	}

	/** get type of service */
	public int tos() {
		return getUInt8(OFFSET_TOS);
	}

	/** set type of service */
	public IpV4Packet tos(int value) {
		putUInt8(OFFSET_TOS, value);
		return this;
	}

	/** get version */
	public int totalLength() {
		return getUInt16(OFFSET_TOTAL_LENGTH);
	}

	/** set version */
	public IpV4Packet totalLength(int value) {
		putUInt16(OFFSET_TOTAL_LENGTH, value);
		return this;
	}

	/** get identification */
	public int id() {
		return getUInt16(OFFSET_ID);
	}

	/** set identification */
	public IpV4Packet id(int value) {
		putUInt16(OFFSET_ID, value);
		return this;
	}

	/** get flags */
	public int flags() {
		return getUInt4(OFFSET_FLAGS, ByteArray.HIGH_BITS);
	}

	/** set version */
	public IpV4Packet flags(int value) {
		putUInt4(OFFSET_FLAGS, ByteArray.HIGH_BITS, value);
		return this;
	}

	/** get fragment offset */
	public int fragment() {
		return getUInt12(OFFSET_FRAGMENT, ByteArray.LOW_BITS);
	}

	/** set fragment offset */
	public IpV4Packet fragment(int value) {
		putUInt12(OFFSET_FRAGMENT, ByteArray.LOW_BITS, value);
		return this;
	}

	/** get TTL */
	public int ttl() {
		return getUInt8(OFFSET_TTL);
	}

	/** set TTL */
	public IpV4Packet ttl(int value) {
		if (value < 0 || value > 255)
			throw new IllegalArgumentException("error TTL value " + value);
		putUInt8(OFFSET_TTL, value);
		return this;
	}

	/** get protocol */
	public int protocol() {
		return getUInt8(OFFSET_PROTOCOL);
	}

	/** set protocol */
	public IpV4Packet protocol(int value) {
		if (value < 0 || value > 255)
			throw new IllegalArgumentException("error protocol number " + value);
		putUInt8(OFFSET_PROTOCOL, value);
		return this;
	}

	/** get checksum */
	public long checksum() {
		return getUInt16(OFFSET_CHECKSUM);
	}

	/** set checksum */
	@Override
	public IpV4Packet checksum(long value) {
		if (value == 0) {
			putUInt16(OFFSET_CHECKSUM, 0);
			value = CheckSum.calcChecksum(array(), arrayOffset(), headerLength()) & 0xFFFF;
		}
		putUInt16(OFFSET_CHECKSUM, (int)value);
		return this;
	}

	/** get options */
	public byte[] options() {
		int length = headerLength() - OFFSET_OPTIONS;
		byte[] ret = new byte[length];
		if (length > 0)
			get(OFFSET_OPTIONS, ret);
		return ret;
	}

	/** set options */
	public IpV4Packet options(byte[] opts) {
		put(OFFSET_OPTIONS, opts);
		// TODO: enlarge header length
		return this;
	}

	/** get source InetAddress */
	public InetAddress srcIp() {
		byte[] bs = getBytes(OFFSET_SRC_IPV4, 4);
		try {
			return Inet4Address.getByAddress(bs);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("unknown host");
		}
	}

	/** put source InetAddress */
	public IpV4Packet srcIp(InetAddress addr) {
		if (addr instanceof Inet4Address) {
			put(OFFSET_SRC_IPV4, addr.getAddress());
		} else {
			throw new IllegalArgumentException("not a IPv4 address");
		}
		return this;
	}

	/** get destination InetAddress */
	public InetAddress dstIp() {
		byte[] bs = getBytes(OFFSET_DST_IPV4, 4);
		try {
			return Inet4Address.getByAddress(bs);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("unknown host");
		}
	}

	/** put destination InetAddress */
	public IpV4Packet dstIp(InetAddress addr) {
		if (addr instanceof Inet4Address) {
			put(OFFSET_DST_IPV4, addr.getAddress());
		} else {
			throw new IllegalArgumentException("not a IPv4 address");
		}
		return this;
	}

	public IpV4Packet init(int size) {
		super.init(size);
		this.version(4).headerLength(HEADER_SIZE);
		this.tos(0).ttl(255);
		return this;
	}



}
