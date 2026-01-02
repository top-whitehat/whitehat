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
 * TCP (Transmission Control Protocol)<br>
 * 
 *
 * <pre>
 
 TCP packet format 
 
 0               1               2               3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|          Source Port          |       Destination Port        |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                        Sequence Number                        |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                    Acknowledgment Number                      |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|  Data |           |U|A|P|R|S|F|                               |
| Offset| Reserved  |R|C|S|S|Y|I|            Window             |
|       |           |G|K|H|T|N|N|                               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|           Checksum            |         Urgent Pointer        |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                    Options (if Data Offset > 5)               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                              Data                             |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

Source Port     : byte  0, 16 bits       , value=0~65535 (the sending port of the application on the source device)
Destination Port: byte  2, 16 bits       , value=0~65535 (the receiving port of the application on the destination device.)
Sequence Number : byte  4, 32 bits       , ​​During Data Transfer, It indicates the byte offset of the first byte of data in this segment within the entire byte stream.
                    During Connection Establishment (SYN flag set):​​ It is the Initial Sequence Number (ISN). The actual first data byte for this connection will be ISN + 1.

ACK Number      : byte  8, 32 bits       , acknowledge received data. It contains the ​​next sequence number that the receiver expects to receive​​.
Data Offset     : byte 12, 4 bits(higher), Header Length which specifies the size of the TCP header in ​​4-byte words​​.

Control Flags:
URG​(Urgent)     : byte 13, 1 bits(offset 5) , Indicates that the ​​Urgent Pointer​​ field is significant. The urgent pointer points to the sequence number of the last byte of urgent data.
ACK(Acknowledgment): byte 13, 1 bits(offset 4) , Indicates that the ​​Acknowledgment Number​​ field is significant. Set to 1 for almost all packets after the initial SYN.
​​PSH​(Push)       : byte 13, 1 bits(offset 3) , Tells the receiving TCP stack to "push" the data immediately to the application without buffering. Used for interactive traffic like keystrokes.
RST​(Reset)      : byte 13, 1 bits(offset 2) , Abruptly resets the connection. Used when a host receives an unexpected packet or when there is an error.
​​SYN(Synchronize): byte 13, 1 bits(offset 1) ,​ Sent in the first step of the three-way handshake to initiate a connection and synchronize sequence numbers.
FIN​(Finish)     : byte 13, 1 bits(offset 0) , Sent to gracefully close a connection, indicating the sender has no more data to send.

Window          : byte 14, 16 bits       , For ​​flow control​​: it indicates the amount of data (in bytes) that the receiver is willing to accept, 
                   starting from the Acknowledgment Number. It tells the sender how much free space remains in its receive buffer.

Checksum        : byte 16, 16 bits       , error-checking​​ the entire TCP segment (header and data)
Urgent Pointer  : byte 18, 16 bits       , an offset from the sequence number, indicating the position of the last byte of urgent data.
Options         : byte 20, variable length (Options data must be a multiple of 32 bits. Padding data if needed.)
 * </pre>
 */
public class TcpPacket extends Layer4Packet {

	/* offsets */
	private final static int OFFSET_SRC_PORT = 0; // 16 bits
	private final static int OFFSET_DST_PORT = 2; // 16 bits
	private final static int OFFSET_SEQ_NUM = 4; // 32 bits
	private final static int OFFSET_ACK_NUM = 8; // 32 bits
	private final static int OFFSET_DATA_OFFSET = 12; // 4 bits

	private final static int OFFSET_FLAGS = 13;
	private final static int BIT_URG = 5;
	private final static int BIT_ACK = 4;
	private final static int BIT_PSH = 3;
	private final static int BIT_RST = 2;
	private final static int BIT_SYN = 1;
	private final static int BIT_FIN = 0;

	private final static int OFFSET_WINDOW = 14; // 16 bits
	private final static int OFFSET_CHECKSUM = 16; // 16 bits
	private final static int OFFSET_URG_PTR = 12; // Urgent Pointer, 16 bits
	private final static int OFFSET_OPTIONS = 20;

	public final static int HEADER_SIZE = 20;

	/** create a TCP packet with specified data length */
	public static TcpPacket create(InetAddress srcIp, int srcPort, InetAddress dstIp, int dstPort, int dataSize) {
		int tcpSize = HEADER_SIZE + dataSize;
		IpPacket ipkt = IpPacket.create(IpPacket.TCP, srcIp, dstIp, tcpSize);
		TcpPacket tcp = ipkt.child(TcpPacket.class);
		tcp.srcPort(srcPort);
		tcp.dstPort(dstPort);
		tcp.dataOffset(HEADER_SIZE / 4);
		tcp.window(10240);
		tcp.seqNumber(1);
		tcp.ackNumber(0);
		return tcp;
	}

	/** create a TCP SYN packet. Send by the client in TCP Three-way Handshake step 1 */
	public static TcpPacket createSyn(InetAddress srcIp, int srcPort, InetAddress dstIp, int dstPort, int sequenceNumber) {
		TcpPacket tcp = TcpPacket.create(srcIp, srcPort, dstIp, dstPort, 0);
		tcp.syn(true).seqNumber(sequenceNumber).checksum(0);
		return tcp;
	}

	/**
	 * create a TCP SYN-ACK reply packet. Send by the server in TCP Three-way Handshake step 2
	 */
	public static TcpPacket replySynAck(TcpPacket req, int seqNumber) {
		TcpPacket tcp = TcpPacket.create(req.dstIp(), req.dstPort(), req.srcIp(), req.srcPort(), 0);
		tcp.ack(true).syn(true).ackNumber(req.seqNumber() + 1);
		tcp.seqNumber(seqNumber).checksum(0);
		return tcp;
	}

	/** create a TCP ACK reply packet. Send by the client in TCP Three-way Handshake step 3 */
	public static TcpPacket replyAck(TcpPacket req, int seqNumber, byte[] data) {
		int len = data == null ? 0 : data.length;
		TcpPacket tcp = TcpPacket.create(req.dstIp(), req.dstPort(), req.srcIp(), req.srcPort(), len);
		tcp.ack(true).ackNumber(req.seqNumber() + 1);
		if (data != null) tcp.payload(data);
		tcp.seqNumber(seqNumber).checksum(0);
		return tcp;
	}
	
	/**
	 * create a TCP RST reply packet.
	 */
	public static TcpPacket replyRst(TcpPacket req, int seqNumber) {
		TcpPacket tcp = TcpPacket.create(req.dstIp(), req.dstPort(), req.srcIp(), req.srcPort(), 0);
		tcp.rst(true).ackNumber(req.seqNumber() + 1);
		tcp.seqNumber(seqNumber).checksum(0);
		return tcp;
	}
	 
	/** create a FIN packet. For the TCP Four-Way Wavehand step 1 */
	public static TcpPacket createFin(InetAddress srcIp, int srcPort, InetAddress dstIp, int dstPort, int sequenceNumber) {
		TcpPacket tcp = TcpPacket.create(srcIp, srcPort, dstIp, dstPort, 0);
		tcp.fin(true).seqNumber(sequenceNumber).checksum(0);
		return tcp;
	}
	
	
	/** create a FIN-ACK packet. For the TCP Four-Way Wavehand step 3 */
	public static TcpPacket replyFinAck(TcpPacket req, int seqNumber) {
		TcpPacket tcp = TcpPacket.create(req.dstIp(), req.dstPort(), req.srcIp(), req.srcPort(), 0);
		tcp.ack(true).fin(true).ackNumber(req.seqNumber() + 1);
		tcp.seqNumber(seqNumber).checksum(0);
		return tcp;
	}

	public TcpPacket() {
		super();
	}

	public TcpPacket(byte[] data) {
		super(data);
	}

	@Override
	public int headerLength() {
		int n = getUInt4(OFFSET_DATA_OFFSET, 7);
		return n * 4;
	}

	@Override
	public TcpPacket headerLength(int value) {
		int m = value % 4;
		if (m != 0) {
			value += (4 - m);
		}
		putUInt4(OFFSET_DATA_OFFSET, 7, value / 4);
		return this;
	}

	/** get source port */
	public int srcPort() {
		return getUInt16(OFFSET_SRC_PORT);
	}

	/** set source port */
	public TcpPacket srcPort(int value) {
		putUInt16(OFFSET_SRC_PORT, value);
		return this;
	}

	/** get destination port */
	public int dstPort() {
		return getUInt16(OFFSET_DST_PORT);
	}

	/** set destination port */
	public TcpPacket dstPort(int value) {
		putUInt16(OFFSET_DST_PORT, value);
		return this;
	}

	/** get sequence number */
	public long seqNumber() {
		return getUInt32(OFFSET_SEQ_NUM);
	}

	/** set sequence number */
	public TcpPacket seqNumber(long value) {
		putUInt32(OFFSET_SEQ_NUM, value);
		return this;
	}

	/** get ACK number */
	public long ackNumber() {
		return getUInt32(OFFSET_ACK_NUM);
	}

	/** set ACK number */
	public TcpPacket ackNumber(long value) {
		putUInt32(OFFSET_ACK_NUM, value);
		return this;
	}

	public int dataOffset() {
		return getUInt4(OFFSET_DATA_OFFSET, 7);
	}

	public TcpPacket dataOffset(int value) {
		putUInt4(OFFSET_DATA_OFFSET, 7, value);
		return this;
	}

	/** get URG flag */
	public boolean urg() {
		return getBit(OFFSET_FLAGS, BIT_URG);
	}

	/** set URG flag */
	public TcpPacket urg(boolean value) {
		putBit(OFFSET_FLAGS, BIT_URG, value);
		return this;
	}

	/** get ACK flag */
	public boolean ack() {
		return getBit(OFFSET_FLAGS, BIT_ACK);
	}

	/** set ACK flag */
	public TcpPacket ack(boolean value) {
		putBit(OFFSET_FLAGS, BIT_ACK, value);
		return this;
	}

	/** get PSH flag */
	public boolean psh() {
		return getBit(OFFSET_FLAGS, BIT_PSH);
	}

	/** set PSH flag */
	public TcpPacket psh(boolean value) {
		putBit(OFFSET_FLAGS, BIT_PSH, value);
		return this;
	}

	/** get RST flag */
	public boolean rst() {
		return getBit(OFFSET_FLAGS, BIT_RST);
	}

	/** set RST flag */
	public TcpPacket rst(boolean value) {
		putBit(OFFSET_FLAGS, BIT_RST, value);
		return this;
	}

	/** get flags */
	public byte flags() {
		return getByte(OFFSET_FLAGS);
	}

	/** set flags */
	public TcpPacket flags(byte value) {
		putByte(OFFSET_FLAGS, value);
		return this;
	}

	/** get SYN flag */
	public boolean syn() {
		return getBit(OFFSET_FLAGS, BIT_SYN);
	}

	/** set SYN flag */
	public TcpPacket syn(boolean value) {
		putBit(OFFSET_FLAGS, BIT_SYN, value);
		return this;
	}

	/** get FIN flag */
	public boolean fin() {
		return getBit(OFFSET_FLAGS, BIT_FIN);
	}

	/** set FIN flag */
	public TcpPacket fin(boolean value) {
		putBit(OFFSET_FLAGS, BIT_FIN, value);
		return this;
	}

	/** get window */
	public int window() {
		return getUInt16(OFFSET_WINDOW);
	}

	/** set window */
	public TcpPacket window(int value) {
		putUInt16(OFFSET_WINDOW, value);
		return this;
	}

	/** get length (including both ​​header and data​​) */
	public int length() {
		return writerIndex(); /// ??
	}

	/** get checksum */
	public long checksum() {
		return getUInt16(OFFSET_CHECKSUM);
	}

	/** calculate checksum */
	protected short calcChecksum(int protocol, int totalLength) {
		int offset = 0;

		long sum = 0; // initialize sum value

		// checksum: pseudo header
		if (parent() instanceof IpV4Packet) {
			// IPv4 pseudo header(12 byte) = srcIp,dstIp(8 byte) + protocol(2 byte) +
			// total_length(2 byte)
			IpV4Packet ipkt = (IpV4Packet) parent();
			offset = ipkt.arrayOffset() + IpV4Packet.OFFSET_SRC_IPV4;
			sum = CheckSum.checksumBytes(sum, ipkt.array(), offset, 8); // srcIP, dstIp(8 byte)
			sum = CheckSum.checksumShort(sum, (short) protocol); // protocol
			sum = CheckSum.checksumShort(sum, (short) totalLength); // total length

		} else if (parent() instanceof IpV6Packet) {
			// IPv6 pseudo header(40 byte) = srcIp, dstIp(32 byte) + total_length(4 byte) +
			// protocol(1 byte) + reserved(3 byte)
			IpV6Packet ipkt = (IpV6Packet) parent();
			offset = ipkt.arrayOffset() + IpV6Packet.OFFSET_SRC_IPV6;
			sum = CheckSum.checksumBytes(sum, ipkt.array(), offset, 32); // srcIP, dstIp(32 byte)
			sum = CheckSum.checksumShort(sum, (short) 0); // (2 byte)
			sum = CheckSum.checksumShort(sum, (short) totalLength); // total length (2 byte)
			sum = CheckSum.checksumShort(sum, (short) ((protocol & 0xFF) << 8)); // protocol + reserved(1 byte)
			sum = CheckSum.checksumShort(sum, (short) 0); // reserved(2 byte)
		}

		// checksum append: header + body
		sum = CheckSum.checksumBytes(sum, array(), arrayOffset(), length());

		// get checksum result
		return CheckSum.checksumResult(sum);
	}

	/** set checksum */
	public TcpPacket checksum(long value) {
		if (value == 0) {
			putUInt16(OFFSET_CHECKSUM, 0);
			value = calcChecksum(IpPacket.TCP, length()) & 0xFFFF;
		}
		putUInt16(OFFSET_CHECKSUM, (int) value);
		if (parent() != null)
			parent().checksum(0);
		return this;
	}

	/** get urgent pointer */
	public int urgentPointer() {
		return getUInt16(OFFSET_URG_PTR);
	}

	/** set urgent pointer */
	public TcpPacket urgentPointer(int value) {
		putUInt16(OFFSET_URG_PTR, value);
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
	public TcpPacket options(byte[] opts) {
		// TODO: enlarge header length
		put(OFFSET_OPTIONS, opts);
		return this;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClassName());
		sb.append(", Src: ").append(getIpPort(true));
		sb.append(", Dst: ").append(getIpPort(false));

		String flags = "";
		if (syn())
			flags += " SYN";
		if (fin())
			flags += " FIN";
		if (ack())
			flags += " ACK";
		if (flags.length() > 0)
			sb.append(", [").append(flags.trim()).append("]");

		sb.append(", Seq=").append(this.seqNumber());
		if (ack())
			sb.append(", Ack=").append(this.ackNumber());
		sb.append(", Win=").append(this.window());

		return sb.toString();
	}

}
