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
import java.net.Inet6Address;
import java.net.InetAddress;

import top.whitehat.Protocols;

/** IP Packet <br>
 */
public abstract class IpPacket extends Layer3Packet {

	private static int OFFSET_VERSION = 0; // 4 bits

	/* protocols */
	
	/** IP in IP */
	public final static int IP_IN_IP = 4;  // code defined in IP packet
	
	/** TCP */
	public final static int TCP = 6;  // code defined in IP packet 

	/** UDP */
	public final static int UDP = 17; // code defined in IP packet 
	
	/** Internet Control Message Protocol */
	public final static int ICMP = 1;  // code defined in IP packet 
	
	/** Internet Control Message Protocol in IPv6 */
	public final static int ICMPV6 = 58;  // code defined in IP packet 
	
	/** Internet Group Management Protocol */
	public final static int IGMP = 2; // 
	
	/** IPv6-in-IPv4 */
	public final static int IPV6_IN_V4 = 41; // 
	
	/** Encapsulating Security Payload, part of IPsec */
	public final static int ESP = 50; //
	
	/** Authentication Header for IPsec */
	public final static int AH = 51; // 
	
	/** Layer 2 Tunneling Protocol */
	public final static int L2TP = 115; //
	
	/** Creates an IPPacket with specified protocol, srcIp, dstIp, specified payload data size */
	public static IpPacket create(int protocol, InetAddress srcIp, InetAddress dstIp, int dataSize) {	
		
		if (srcIp == null && dstIp == null) {
			return IpV4Packet.createNew(protocol, srcIp, dstIp, dataSize);
			
		} else if (dstIp instanceof Inet4Address || srcIp instanceof InetAddress) {
			return IpV4Packet.createNew(protocol, srcIp, dstIp, dataSize);
			
		} else if (dstIp instanceof Inet6Address || srcIp instanceof Inet6Address) {
			return IpV6Packet.createNew(protocol, srcIp, dstIp, dataSize);
			
		} else {
			throw new IllegalArgumentException("dstIp and srcIp should be same InetAddress");
		}
	}
	
	/** Creates an IPPacket with specified protocol, srcIp, dstIp, specified data bytes */
	public static IpPacket create(int protocol, InetAddress srcIp, InetAddress dstIp, byte[] data) {
		int dataSize = data == null ? 0 : data.length;
		IpPacket ret = create(protocol, srcIp, dstIp, dataSize);
		if (data!=null) ret.payload(data);		
		return ret;
	}

	/** Wrap a byte array from a specified offset and length, return an IpPacket.
	 *  
	 * @param array    The byte array
	 * @param offset   The start offset
	 * @param length   Length of bytes
	 * 
	 * @return An IpPacket
	 */
	public static IpPacket wrap(byte[] array, int offset, int length) {
		int version = (array[offset] & 0xF0) >> 4;
		if (version == 4) {
			return IpV4Packet.wrap(array, offset, length);
		} else if (version == 4) {
			return IpV4Packet.wrap(array, offset, length);
		} else {
			throw new IllegalArgumentException("not a Ip packet data");
		}
	}
	
	/** Wrap a byte array, return an IpPacket.
	 * 
	 * @param data  The byte array
	 * @return An IpPacket
	 */
	public static IpPacket wrap(byte[] data) {
		return wrap(data, 0, -1);
	}
	
	/** constructor */
	IpPacket() {
		super();
	}

	/** constructor */
	IpPacket(byte[] buf) {
		super(buf);
	}

	/** get version */
	public int version() {
		return getUInt4(OFFSET_VERSION, 7);
	}

	/** set version */
	public IpPacket version(int value) {
		putUInt4(OFFSET_VERSION, 7, value);
		return this;
	}

	/** get TTL */
	public int ttl() {
		return 0;
	}

	/** set TTL */
	public IpPacket ttl(int value) {
		return this;
	}

	/** get protocol */
	public int protocol() {
		return 0;
	}

	/** set protocol */
	public IpPacket protocol(int value) {
		return this;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClassName());
		sb.append(", Src: ").append(getIpString(true));
		sb.append(", Dst: ").append(getIpString(false));
		sb.append(", Protocol: ").append(Protocols.getProtocolName(protocol()));
		return sb.toString();
	}

	@Override
	public Packet child() {
		int p = protocol();
		switch (p) {
		case TCP: 
			return child(TcpPacket.class).child();
		case UDP: 
			return child(UdpPacket.class).child();
		case ICMP: 
			return child(IcmpPacket.class).child();
		default:
			break;
		}
		return this;
	}
	
	public UdpPacket getUdpPacket() {
		if (protocol() == UDP) {
			return child(UdpPacket.class);
		}
		return null;
	}

	public TcpPacket getTcpPacket() {
		if (protocol() == TCP) {
			return child(TcpPacket.class);
		}
		return null;
	}
	
}
