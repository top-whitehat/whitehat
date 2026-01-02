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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/** IPv6 Packet <br>
 * 
 *
 <pre>
 
 IPv6 packet format 
 
 0               1               2               3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|Version| Traffic Class  |          Flow Label                  |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|         Payload Length        |  Next Header   |   Hop Limit   |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                Source IP Address (128 bits)                   |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|              Destination Address (128 bits)                   |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                Extension Headers (Optional)                   |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|           Payload (Upper-Layer Protocol Data)                 |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

Version        : byte  0,  4 bits(high) , value=6 (IPv6)
Traffic Class  : byte  0,  8 bits(lower), (QoS priority, similar to IPv4 DS field)
Flow Label     : byte  1, 20 bits       , (Packet flow identification, for real-time applications)
Payload Length : byte  4, 16 bits       , (Length of data after header)
Next Header​    : byte  6,  8 bits       , value=6(TCP), 17(UDP), etc. (Protocol number)
Hop Limit​      : byte  7,  8 bits       , (router hops, decremented each hop, like IPv4 TTL)
Source IP      : byte  8,128 bits       , value=16 bytes (Source IPv6 address) 
Destination IP : byte 24, 32 bits       , value=16 bytes (Destination IPv6 address)
 </pre>
 */
public class IpV6Packet extends IpPacket {
	
	private static int OFFSET_NEXT_HEADER = 6; // 8 bits
	private static int OFFSET_HOP_LIMIT = 7;   // 8 bits  		 
	public static int OFFSET_SRC_IPV6 = 8;    // 128 bits
	private static int OFFSET_DST_IPV6 = 24;   // 128 bits
	private static int OFFSET_EXT_HEADER = 40; 
	
	public final static int HEADER_SIZE = 40;
	
	/** Create an IPv6Packet with specified protocol, srcIp, dstIp, specified payload
	 * data size
	 */
	protected static IpV6Packet createNew(int protocol, InetAddress srcIp, InetAddress dstIp, int dataSize) {
		int totalSize = IpV6Packet.HEADER_SIZE + dataSize;
		IpV6Packet p = new IpV6Packet();
		p.init(totalSize);
		p.version(6).ttl(0xFF).protocol(protocol);
		if (srcIp != null) p.srcIp(srcIp);
		if (dstIp != null) p.dstIp(dstIp);
		p.checksum(0);
		return p;
	}
	
	/** Wrap a byte array from a specified offset and length, return an IpV6Packet. */
	public static IpPacket wrap(byte[] data, int offset, int length) {
		IpV6Packet p = new IpV6Packet();
		p.array(data, offset, length);
		return p;
	}
	
	/** Constructor */
	public IpV6Packet() {
		super();
	}
	
	/** Constructor */
	public IpV6Packet(byte[] buf) {
		super(buf);
	}
	
	/** Returns the header length in bytes. */
	public int headerLength() {
		return OFFSET_EXT_HEADER;
	}
	
	/** Returns the packet length in bytes. */
	public Packet headerLength(int len) {
		return this;
	}
		
	/** get TTL */
	public int ttl() {
		return getUInt8(OFFSET_HOP_LIMIT);
	}
	
	/** set TTL */
	public IpPacket ttl(int value) {
		if (value < 0 || value > 255) throw new IllegalArgumentException("error TTL value " + value);
		putUInt8(OFFSET_HOP_LIMIT, value);
		return this;
	}
	
	/** get protocol */
	public int protocol() {
		return getUInt8(OFFSET_NEXT_HEADER);
	}
	
	/** set protocol */
	public IpPacket protocol(int value) {
		if (value < 0 || value > 255) throw new IllegalArgumentException("error protocol number " + value); 
		putUInt8(OFFSET_NEXT_HEADER, value);
		return this;
	}
	
	/** get source InetAddress */
	@Override
	public InetAddress srcIp() {
		byte[] bs = getBytes(OFFSET_SRC_IPV6, 16);
		try {
			return Inet6Address.getByAddress(bs);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("unknown host");
		}
	}
	
	/** put source InetAddress */
	@Override
	public IpPacket srcIp(InetAddress addr) {
		if (addr instanceof Inet6Address) {
			put(OFFSET_SRC_IPV6, addr.getAddress());
		} else {
			throw new IllegalArgumentException("not a IPv6 address");
		}
		return this;
	}
	
	/** get destination InetAddress */
	@Override
	public InetAddress dstIp() {
		byte[] bs = getBytes(OFFSET_DST_IPV6, 16);
		try {
			return Inet6Address.getByAddress(bs);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("unknown host");
		}
	}
	
	/** put destination InetAddress */
	@Override
	public IpPacket dstIp(InetAddress addr) {
		if (addr instanceof Inet6Address) {
			put(OFFSET_DST_IPV6, addr.getAddress());
		} else {
			throw new IllegalArgumentException("not a IPv6 address");
		}
		return this;
	}
		
	
	public IpPacket init(int size) {
		super.init(size);
		this.version(6).headerLength(HEADER_SIZE);
		this.ttl(255);
		return this;
	}

}
