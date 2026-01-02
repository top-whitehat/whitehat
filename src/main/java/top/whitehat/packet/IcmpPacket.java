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
import java.time.Instant;

/**
 * ICMP protocol <br>
 * 
 *
 * <pre>
 
 ICMP packet format 
 
 0               1               2               3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|     Type      |     Code      |          Checksum             |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                     Rest of the Header                        |
|         (varies depending on ICMP Type/Code)                  |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                     Data (optional/payload)                   |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

Type            : byte  0, 8 bits       , value=0​​(Echo Reply), 8​​(Echo Request), etc.  (the kind of ICMP message.)
Code            : byte  1, 8 bits       , value= (additional information about the ICMP message type.)
					The meaning of the code depends on the ICMP Type.
						For ​​Type 3 (Destination Unreachable)​​, codes include:
							0: Network unreachable
							1: Host unreachable
							2: Protocol unreachable
							3: Port unreachable
							etc.
Checksum        : byte  2, 16 bits      , error-checking​​ the entire header and data
Rest of the Header:byte 4, variable, 32 bits or more, Contains fields specific to the ICMP type.
		Examples:
			For ​​Echo Request/Reply (Types 8 and 0)​​:
			​​Identifier (16 bits)​​: Helps match requests and replies (often process ID).
			​​Sequence Number (16 bits)​​: Used to match multiple pings.
			​​Data (optional)​​: Often includes a timestamp or padding for echo requests/replies.
			For ​​Time Exceeded (Type 11)​​ or ​​Destination Unreachable (Type 3)​​:
			May include parts of the original IP packet that caused the error.
Data (variable)​:  Contains additional information depending on the ICMP message.
	In ​​Echo Request/Reply​​, this is often just padding or a timestamp.
	For ​​error messages (e.g., Destination Unreachable)​​, it may contain the ​​first 8 bytes 
	of the original IP packet’s data​​ to help identify the problematic connection.
	
	Data of Echo Request:
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |   Identifier(2 bytes)         |    Sequence Number (2 bytes)  |             
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                     Data                                      |
      |  (varies, the data will be same in the echo reply)            |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      
      
 * </pre>
 */
public class IcmpPacket extends Layer4Packet {

	public static final int HEADER_SIZE = 4;

	private static final int OFFSET_TYPE = 0;
	private static final int OFFSET_CODE = 1;
	private static final int OFFSET_CHECKSUM = 2;

	/** in echo request/reply */
	private static final int OFFSET_ID = 4;
	private static final int OFFSET_SEQUENCE = 6;
	private static final int OFFSET_ECHO_DATA = 8;
	
	
	private static final int OFFSET_TIMESTAMP = 8;
	
	/** index of timestamp */
	private static final int TIMESTAMP_ORGINATE = 0;
	private static final int TIMESTAMP_RECEIVE = 1;
	private static final int TIMESTAMP_TRANSMIT = 2;

	/* ICMP Types */
	public final static int ECHO_REQUEST = 8; // Echo Request: Ping request
	public final static int ECHO_REPLY = 0; // Echo Reply: Response to a ping
	public final static int UNREACHABLE = 3; // Destination Unreachable: Packet couldn't be delivered
	public final static int SOURCE_QUECH = 4; // Source Quench: Congestion control (rarely used)
	public final static int REDIRECT = 5; // Redirect: Tell host to use a better route
	public final static int TIME_EXCEEDED = 11; // Time Exceeded: TTL expired in transit
	public final static int PARAM_PROBLEM = 12; // Parameter Problem: Malformed IP packet
	public final static int TIMESTAMP_REQUEST = 13; // Timestamp Request: Time sync (rare)
	public final static int TIMESTAMP_REPLY = 14; // Timestamp Reply: Time sync reply
	public final static int MASK_REQUEST = 17; // Address Mask Request: Subnet mask query
	public final static int MASK_REPLY = 18; // Address Mask Reply: Subnet mask response

	/** create byte array of specified length */
	protected static byte[] createEchoData(int len) {
		byte[] ret = new byte[len];
		byte c = 0x61;
		for (int i = 0; i < len; i++) {
			ret[i] = c;
			c++;
			if (c > 0x77)
				c = 0x61;
		}
		return ret;
	}

	/** echo data */
	protected final static byte[] echoData32 = createEchoData(32);

	/** Create a ICMP echo request packet (ping)
	 * 
	 * @param dstIp     The destination InetAddress
	 * @param srcIp     The source InetAddress
	 * @param id        Echo request id
	 * @param sequence  Sequence number
	 * @param dataSize  Data size
	 * @param ttl       Time to live
	 * 
	 * @return IcmpPacket object
	 */
	public static IcmpPacket request(InetAddress dstIp, InetAddress srcIp, int id, int sequence, int dataSize, int ttl) {
		// prepare data bytes
		if (dataSize < 0)
			dataSize = 32;
		byte[] echoData = dataSize == 32 ? echoData32 : createEchoData(dataSize);
		int icmpSize = HEADER_SIZE + 4 + dataSize;

		// create IPPacket
		IpPacket ipkt = IpPacket.create(IpPacket.ICMP, srcIp, dstIp, icmpSize);
		IcmpPacket icmp = ipkt.child(IcmpPacket.class);

		// write ICMPPacket header
		icmp.type(ECHO_REQUEST).code(0);

		// write echo request id, sequence, data
		icmp.id(id).sequence(sequence).echoData(echoData);
		icmp.checksum(0); // update ICMP checksum
		ipkt.ttl(ttl);
		ipkt.checksum(0); // update IPPacket checksum

		return icmp;
	}

	/** Create a ICMP echo reply packet
	 * 
	 * @param dstIp     the destination InetAddress
	 * @param srcIp     the source InetAddress
	 * @param id        echo request id
	 * @param sequence  sequence number
	 * @param echoData  echo data
	 * @return IcmpPacket object
	 */
	public static IcmpPacket reply(InetAddress dstIp, InetAddress srcIp, int id, int sequence, byte[] echoData) {
		int icmpSize = HEADER_SIZE + 4 + echoData.length;

		// create IPPacket
		IpPacket ipkt = IpPacket.create(IpPacket.ICMP, srcIp, dstIp, icmpSize);
		IcmpPacket icmp = ipkt.child(IcmpPacket.class);

		// write ICMPPacket header
		icmp.type(ECHO_REPLY).code(0);

		// write echo request id, sequence, data
		icmp.id(id).sequence(sequence).echoData(echoData);
		icmp.checksum(0); // update ICMP checksum
		ipkt.checksum(0); // update IPPacket checksum

		return icmp;
	}

	/** Create a ICMP echo reply to specified request
	 * 
	 * @param request   The ICMP echo request packet
	 * 
	 * @return IcmpPacket object
	 */
	public static IcmpPacket reply(IcmpPacket request) {
		if (request.type() != ECHO_REQUEST)
			return null;

		return reply(request.srcIp(), request.dstIp(), request.id(), request.sequence(), request.echoData());
	}

	/** Create a ICMP timestamp request packet
	 * 
	 * @param dstIp     the destination InetAddress
	 * @param srcIp     the source InetAddress
	 * @param id        echo request id
	 * @param sequence  sequence number
	 * @param originateTimestamp originate timestamp
	 * @return
	 */
	public static IcmpPacket timestampRequest(InetAddress dstIp, InetAddress srcIp, 
				int id, int sequence, Instant originateTimestamp) {
		// prepare data bytes
		if (originateTimestamp == null) originateTimestamp = Instant.now();
		int dataSize = 12; // Originate Timestamp(4-byte) +  Receive Timestamp(4-byte) +  Transmit Timestamp(4-byte) 		
		int icmpSize = HEADER_SIZE + 4 + dataSize;
		
		// create IPPacket
		IpPacket ipkt = IpPacket.create(IpPacket.ICMP, srcIp, dstIp, icmpSize);		
		IcmpPacket icmp = ipkt.child(IcmpPacket.class);
		
		// write ICMPPacket header
		icmp.type(TIMESTAMP_REQUEST).code(0);
		
		// write echo request id, sequence
		icmp.id(id).sequence(sequence); 
		icmp.timestamp(TIMESTAMP_ORGINATE, originateTimestamp); // write originate timestamp
		icmp.checksum(0); // update ICMP checksum
		ipkt.checksum(0); // update IPPacket checksum
		
		return icmp;
	}
	
	/** Create a ICMP timestamp reply packet
	 * 
	 * @param request            The ICMP timestamp request
	 * @param receiveTimestamp   Receive timestamp
	 * @param transmitTimestamp  Transmit timestamp
	 * @return
	 */
	public static IcmpPacket timestampReply(IcmpPacket request, Instant receiveTimestamp, Instant transmitTimestamp) {
		if (request.type() != TIMESTAMP_REQUEST)
			return null;
		
		Instant originateTimestamp = request.timestamp(TIMESTAMP_ORGINATE);
		if(receiveTimestamp == null) receiveTimestamp = Instant.now();
		if(transmitTimestamp == null) transmitTimestamp = Instant.now();

		IcmpPacket icmp = reply(request.srcIp(), request.dstIp(), request.id(), request.sequence(), new byte[12]);
		IpPacket ipkt = (IpPacket)icmp.parent();
		
		icmp.timestamp(TIMESTAMP_ORGINATE, originateTimestamp); // write originate timestamp
		icmp.timestamp(TIMESTAMP_RECEIVE, receiveTimestamp); // write receiveTimestamp
		icmp.timestamp(TIMESTAMP_TRANSMIT, transmitTimestamp); // write transmitTimestamp
		icmp.checksum(0); // update ICMP checksum
		ipkt.checksum(0); // update IPPacket checksum
		return icmp;
	}


	/**  Create a empty IcmpPacket */
	public IcmpPacket() {
		super();
	}

	/** Create IcmpPacket by reading specified data bytes*/
	public IcmpPacket(byte[] data) {
		super(data);
	}

	@Override
	public int headerLength() {
		return HEADER_SIZE;
	}

	@Override
	public IcmpPacket headerLength(int value) {
		if (value < HEADER_SIZE)
			throw new IllegalArgumentException("Length of ICMPPacket should great than " + HEADER_SIZE);
		return this;
	}

	/** Get the value of type */
	public int type() {
		return getUInt8(OFFSET_TYPE);
	}

	/** Set the value of type */
	public IcmpPacket type(int value) {
		putUInt8(OFFSET_TYPE, value);
		return this;
	}

	/** Check whether this ICMP packet is a request */
	public boolean isRequest() {
		return type() == ECHO_REQUEST || type() == TIMESTAMP_REQUEST;
	}

	/** Check whether this ICMP packet is a reply */
	public boolean isReply() {
		return type() == ECHO_REPLY || type() == TIMESTAMP_REPLY;
	}

	/** Get the value of code */
	public int code() {
		return getUInt8(OFFSET_CODE);
	}

	/** Set the value of code */
	public IcmpPacket code(int value) {
		putUInt8(OFFSET_CODE, value);
		return this;
	}

	/** Get the value of checksum */
	public long checksum() {
		return getUInt16(OFFSET_CHECKSUM);
	}

	/** Set the value of checksum */
	public IcmpPacket checksum(long value) {
		if (value == 0) {
			// set checksum value to 0 before calculate checksum
			putUInt16(OFFSET_CHECKSUM, 0);
			// calculate checksum
			value = CheckSum.calcChecksum(array(), arrayOffset(), arrayLength() - arrayOffset());
		}
		putUInt16(OFFSET_CHECKSUM, (int)value);
		return this;
	}

	/** Get the value of id */
	public int id() {
		return getUInt16(OFFSET_ID);
	}

	/** Set the value of id */
	public IcmpPacket id(int value) {
		putUInt16(OFFSET_ID, value);
		return this;
	}

	/** Get the value of sequence */
	public int sequence() {
		return getUInt16(OFFSET_SEQUENCE);
	}

	/** Set the value of sequence */
	public IcmpPacket sequence(int value) {
		putUInt16(OFFSET_SEQUENCE, value);
		return this;
	}

	/** Get the length of echo data */
	public int getEchoDataLength() {
		return writerIndex() - OFFSET_ECHO_DATA;
	}

	/** Get echo data */
	public byte[] echoData() {
		return getBytes(OFFSET_ECHO_DATA, getEchoDataLength());
	}

	/** Set echo data */
	public IcmpPacket echoData(byte[] value) {
		putBytes(OFFSET_ECHO_DATA, value);
		return this;
	}

	/** Get timestamp */
	public Instant timestamp(int index) {
		long milli = getUInt32(OFFSET_TIMESTAMP + index * 4);
		return Instant.ofEpochMilli(milli);
	}

	/** Set echo data */
	public IcmpPacket timestamp(int index, Instant value) {
		long milli = value.toEpochMilli();
		putUInt32(OFFSET_TIMESTAMP + index * 4, milli);
//        putBytes(OFFSET_ECHO_DATA + index * 4, value);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClassName());

		if (type() == ECHO_REQUEST) {
			sb.append(", echo request");
			sb.append(", Src: ").append(getIpString(true));
			sb.append(", Dst: ").append(getIpString(false));
			sb.append(", id: ").append(id());
			sb.append(", sequence: " + sequence());
			sb.append(", bytes: " + getEchoDataLength());
		} else if (type() == ECHO_REPLY) {
			sb.append(", echo reply  ");
			sb.append(", Src: ").append(getIpString(true));
			sb.append(", Dst: ").append(getIpString(false));
			sb.append(", id: ").append(id());
			sb.append(", sequence: " + sequence());
			sb.append(", bytes: " + getEchoDataLength());
		} else {
			sb.append(", type: ").append(type());
			sb.append(", code: ").append(code());
			sb.append(", Src: ").append(getIpString(true));
			sb.append(", Dst: ").append(getIpString(false));
		}
		return sb.toString();
	}

	@Override
	public IcmpPacket init(int size) {
		super.init(size);
		return this;
	}

}