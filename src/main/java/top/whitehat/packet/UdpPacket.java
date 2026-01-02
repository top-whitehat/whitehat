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

import java.net.InetAddress;

/**
 * UDP (User Datagram Protocol) <br>
 * 
 *
 * <pre>
 
 UDP packet format 
 
 0               1               2               3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|          Source Port          |       Destination Port        |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|             Length            |           Checksum            |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                         Data (Payload)                        |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

Source Port     : byte  0, 16 bits       , value=0~65535 (the sending port of the application on the source device)
Destination Port: byte  2, 16 bits       , value=0~65535 (the receiving port of the application on the destination device.)
Length          : byte  4, 16 bits       , ​​Total length of the UDP datagram in bytes, including both ​​header and data​​.
Checksum        : byte  6, 16 bits       , error-checking​​ the entire header and data
 * </pre>
 */
public class UdpPacket extends Layer4Packet {

	private final static int OFFSET_SRC_PORT = 0; // 16 bits
	private final static int OFFSET_DST_PORT = 2; // 16 bits
	private final static int OFFSET_LENGTH = 4; // 16 bits
	private final static int OFFSET_CHECKSUM = 6; // 16 bits

	public final static int HEADER_SIZE = 8;

	/** create a UDPPacket with specified data length */
	public static UdpPacket create(InetAddress srcIp, int srcPort, InetAddress dstIp, int dstPort, int dataSize) {
		int udpSize = HEADER_SIZE + dataSize;
		IpPacket ipkt = IpPacket.create(IpPacket.UDP, srcIp, dstIp, udpSize);
		UdpPacket udp = ipkt.child(UdpPacket.class);
		udp.length(udpSize);
		udp.srcPort(srcPort).dstPort(dstPort);
		return udp;
	}

	/** create a UDPPacket */
	public static UdpPacket create(InetAddress srcIp, int srcPort, InetAddress dstIp, int dstPort, byte[] data) {
		UdpPacket udp = create(srcIp, srcPort, dstIp, dstPort, data.length);
		udp.payload(data);
		udp.checksum(0);
		return udp;
	}
	
	/** create a reply UDPPacket */
	public static UdpPacket reply(UdpPacket request, byte[] payload) {
		return create(request.dstIp(), request.dstPort(), request.srcIp(), request.srcPort(), payload);
	}

	public UdpPacket() {
		super();
	}

	public UdpPacket(byte[] data) {
		super(data);
	}

	@Override
	public int headerLength() {
		return HEADER_SIZE;
	}

	@Override
	public UdpPacket headerLength(int value) {
		if (value != HEADER_SIZE)
			throw new IllegalArgumentException("Length of UDPPacket should be 8");
		return this;
	}

	/** get source port */
	public int srcPort() {
		return getUInt16(OFFSET_SRC_PORT);
	}

	/** set source port */
	public UdpPacket srcPort(int value) {
		putUInt16(OFFSET_SRC_PORT, value);
		return this;
	}

	/** get destination port */
	public int dstPort() {
		return getUInt16(OFFSET_DST_PORT);
	}

	/** set destination port */
	public UdpPacket dstPort(int value) {
		putUInt16(OFFSET_DST_PORT, value);
		return this;
	}

	/** get length (including both ​​header and data​​) */
	public int length() {
		return getUInt16(OFFSET_LENGTH);
	}

	/** set length (including both ​​header and data​​) */
	public UdpPacket length(int value) {
		putUInt16(OFFSET_LENGTH, value);
		return this;
	}

	/** get checksum */
	public long checksum() {
		return getUInt16(OFFSET_CHECKSUM);
	}

	/** calculate checksum */
	protected short calcChecksum(int protocol, int totalLength) {
		int offset = 0;

		long sum = 0; // init sum value

		// checksum: pseudo header
		if (parent() instanceof IpV4Packet) {
			// IPv4 pseudo header(12 byte) = srcIp,dstIp(8 byte) + protocol(2 byte) +
			// total_length(2 byte)
			IpV4Packet ipkt = (IpV4Packet) parent();

//			ipkt.putUInt16(IpV4Packet.OFFSET_ID, 0xE327);
			ipkt.id(0xE327);
			ipkt.ttl(0x40);

			offset = ipkt.arrayOffset() + IpV4Packet.OFFSET_SRC_IPV4;
			sum = CheckSum.checksumBytes(sum, ipkt.array(), offset, 8); // srcIP, dstIp(8 byte)
			sum = CheckSum.checksumShort(sum, (short) protocol); // protocol
			sum = CheckSum.checksumShort(sum, (short) totalLength); // total length

		} else if (parent() instanceof IpV6Packet) {
			// IPv6 pseudo header(40 byte) = srcIp,dstIp(32 byte) + total_length(4 byte) +
			// protocol(1 byte) + reserved(3 byte)
			IpV6Packet ipkt = (IpV6Packet) parent();
			offset = ipkt.arrayOffset() + IpV6Packet.OFFSET_SRC_IPV6;
			sum = CheckSum.checksumBytes(sum, ipkt.array(), offset, 32); // srcIP, dstIp(32 byte)
			sum = CheckSum.checksumShort(sum, (short) 0); // (2 byte)
			sum = CheckSum.checksumShort(sum, (short) totalLength); // total length (2 byte)
			sum = CheckSum.checksumShort(sum, (short) ((protocol & 0xFF) << 8)); // protocol + reserved(1 byte)
			sum = CheckSum.checksumShort(sum, (short) 0); // reserved(2 byte)
		}

		// checksum: srcPort,dstPort(4byte) + length(2byte) + 0(2byte)
		sum = CheckSum.checksumBytes(sum, array(), arrayOffset(), 4); // srcPort,dstPort(4 byte)
		sum = CheckSum.checksumShort(sum, (short) totalLength); // total length
		sum = CheckSum.checksumShort(sum, (short) 0); // checksum 0

		// checksum append : payload bytes
		offset = arrayOffset() + HEADER_SIZE;
		int payloadLength = arrayLength() - arrayOffset() - HEADER_SIZE;
		sum = CheckSum.checksumBytes(sum, array(), offset, payloadLength);

		// get checksum result
		return CheckSum.checksumResult(sum);
	}

	/** set checksum */
	public UdpPacket checksum(long value) {
		if (value == 0) {
			putUInt16(OFFSET_CHECKSUM, 0);
			value = calcChecksum(IpPacket.UDP, length()) & 0xFFFF;
		}
		putUInt16(OFFSET_CHECKSUM, (int) value);
		if (parent() != null)
			parent().checksum(0);
		return this;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClassName());
		sb.append(", Src: ").append(getIpPort(true));
		sb.append(", Dst: ").append(getIpPort(false));
		return sb.toString();
	}

	@Override
	public UdpPacket payload(byte[] data) {
		super.payload(data);
		length(data.length + HEADER_SIZE);
		return this;
	}

}
