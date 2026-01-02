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
package top.whitehat.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import top.whitehat.packet.IMessage;
import top.whitehat.packet.Layer7Packet;
import top.whitehat.packet.PacketException;
import top.whitehat.packet.PacketUtil;
import top.whitehat.packet.UdpPacket;
import top.whitehat.util.ByteArray;
import top.whitehat.util.IReadWriteByteArray;

/**
 * DNS protocol
 * 
 * <h3>Header format</h3>
 * <a href="https://www.rfc-editor.org/rfc/rfc1035#section-4.1.1"> SEE: RFC1035
 * Header </a>
 * 
 * <pre>
 *     
      0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      ID                       |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    QDCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ANCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    NSCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ARCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
where:

ID       uint16    Identifier
QR       1 bit     Whether this message is a query (0), or a response (1).
OPCODE   4 bit     Kind of query
AA       1 bit     Authoritative Answer
TC       1 bit     TrunCation
RD       1 bit     Recursion Desired

RA       1 bit     Recursion Available
Z        3 bit     Reserved for future use.
RCODE    4 bit     Response code, NO_ERROR(0), Format Error(1), Failure(2) ...

QDCOUNT  uint16    The number of entries in the question section.
ANCOUNT  uint16    The number of resource answer records in the answer section.
NSCOUNT  uint16    The number of name server resource records in the authority records section.
ARCOUNT  uint16    The number of resource records in the additional records section.
 * </pre>
 */
public class DnsPacket extends Layer7Packet implements IMessage {

	/** Query Type: Query class : Internet */
	protected final static int CLASS_INTERNET = 1;

	protected final static int CLASS_CSNET = 2;

	protected final static int CLASS_CHAOS = 3;

	protected final static int CLASS_HESIOD = 4;

	protected final static int CLASS_ALL = 255;

	public static final int HEADER_SIZE = 12;

	private static final int OFFSET_ID = 0;
	private static final int OFFSET_QR = 2;
	private static final int OFFSET_OP_CODE = 2;
	private static final int OFFSET_AA = 2;
	private static final int OFFSET_TC = 2;
	private static final int OFFSET_RD = 2;
	private static final int OFFSET_RA = 3;
	private static final int OFFSET_Z1 = 3;
	private static final int OFFSET_Z2 = 3;
	private static final int OFFSET_RCODE = 3;
	private static final int OFFSET_QD_COUNT = 4;
	private static final int OFFSET_AN_COUNT = 6;
	private static final int OFFSET_NS_COUNT = 8;
	private static final int OFFSET_QR_COUNT = 10;
	
//	/** Convert a UDPPacket to DNS */
//	public static DnsPacket wrap1(UdpPacket udp) {
//		DnsPacket msg = udp.child(DnsPacket.class);
//		return msg;
//	}
	
	/**
	 * Create an DNS query message
	 * 
	 * @param id     id
	 * @param domain domain name
	 * @return DnsPacket
	 */
	public static DnsPacket create(int queryType, int id, String domain) {
		DnsPacket msg = new DnsPacket();
		msg.capacity(1024);
		msg.id(id);
		msg.setResponse(false);

		DnsQuestion q = new DnsQuestion(domain, queryType, DnsPacket.CLASS_INTERNET);
		msg.questions.add(q);		
		msg.buildMessage();
		return msg;
	}
	
	/**
	 * Create an query message
	 * 
	 * @param srcIp      Source IP address
	 * @param srcPort    Source port
	 * @param dstIp      Destination IP address
	 * @param dstPort    Destination port
	 * @param id         Message id
	 * @param type       Query type
	 * @param domain     The domain name to query
	 * 
	 * @return DnsPacket
	 */
	public static DnsPacket query(InetAddress srcIp, int srcPort, InetAddress dstIp, int dstPort, int id, int queryType, String domain) {
		DnsPacket msg = new DnsPacket();
		msg.capacity(1024);
		msg.id(id);
		msg.setResponse(false);

		DnsQuestion q = new DnsQuestion(domain, queryType, DnsPacket.CLASS_INTERNET);
		msg.questions.add(q);		
		msg.buildMessage();
	
		byte[] data = msg.getBytes(0, msg.writerIndex());
		UdpPacket udp = UdpPacket.create(srcIp, srcPort, dstIp, dstPort, data);		
		return udp.child(DnsPacket.class);
	}
	
	/**
	 * Create an request message
	 * 
	 * @param srcIp      Source IP address
	 * @param srcPort    Source port
	 * @param dnsServer  DNS server address
	 * @param id     Message id
	 * @param type   Query type
	 * @param domain The domain name to query
	 * 
	 * @return DnsPacket
	 */
	public static DnsPacket request(InetAddress srcIp, int srcPort, InetAddress dnsServer, int id, int queryType, String domain) {
		return query(srcIp, srcPort, dnsServer, DNS.PORT, id, queryType, domain);
	}
	
	/**
	 * Create an A query message
	 * 
	 * @param id     id
	 * @param type   query type
	 * @param domain domain name
	 * 
	 * @return DnsPacket
	 */
	public static DnsPacket requestA(InetAddress srcIp, int srcPort, InetAddress dnsServer, int id, String domain) {
		DnsPacket msg = new DnsPacket();
		msg.capacity(1024);
		msg.id(id);
		msg.setResponse(false);

		DnsQuestion q = new DnsQuestion(domain, DnsQueryType.A.getValue(), DnsPacket.CLASS_INTERNET);
		msg.questions.add(q);		
		msg.buildMessage();
	
		byte[] data = msg.getBytes(0, msg.writerIndex());
		UdpPacket udp = UdpPacket.create(srcIp, srcPort, dnsServer, DNS.PORT, data);		
		return udp.child(DnsPacket.class);  //DnsPacket.wrap(udp);
	}
	
	/** Create a response message for A Query
	 * 
	 * @param query  The DNS query packet 
	 * @param name   The domain name
	 * @param addr   The ip address of the domain name
	 * @param ttl    time to live
	 * 
	 * @return DnsPacket
	 */
	public static DnsPacket responseA(DnsPacket query, String name, InetAddress addr, long ttl) {
		return query.responseA(name, addr, ttl);
	}

	/** Create a response message for PTR Query
	 * 
	 * @param query  The DNS query packet 
	 * @param name   The domain name
	 * @param addr   The ip address of the domain name
	 * @param ttl    time to live
	 * 
	 * @return DnsPacket
	 */
	public static DnsPacket responsePTR(DnsPacket query, String name, String ptr, long ttl) {
		return query.responsePTR(name, ptr, ttl);
	}

	/** Default TTL in seconds */
	public static final int DEFAULT_TTL = 600;

	/** ax question count in DNS message */
	private static int MAX_COUNT = 40;

	/** question list */
	public List<DnsQuestion> questions = new ArrayList<DnsQuestion>();

	/** IP addresses in DNS response message */
	public List<InetAddress> addresses = new ArrayList<InetAddress>();

	/** cname list in DNS response message */
	public List<String> cnames = new ArrayList<String>();

	/** Resource Records */
	public List<DnsResourceRecord> RRs = new ArrayList<DnsResourceRecord>();

	/** Create an empty DnsPacket */
	public DnsPacket() {
		super();
	}

	/** Create a DnsPacket from specified data */
	public DnsPacket(byte[] data) {
		super(data);
		parseMessage();
	}
	
	/** query domain name (the first question) */
	public String queryName() {
		if (questions.size() > 0)
			return questions.get(0).name;
		return "";
	}

	/** query type (the first question) */
	public int queryType() {
		if (questions.size() > 0)
			return questions.get(0).type;
		return 0;
	}

	/** return name string of query type */
	public String queryTypeName() {
		return DnsQueryType.of(queryType()).toString();
	}
	
	/** query class (the first question) */
	public int queryClass() {
		if (questions.size() > 0)
			return questions.get(0).clazz;
		return 0;
	}

	@Override
	public void parseMessage() {
		questions.clear();
		addresses.clear();
		cnames.clear();
		RRs.clear();
		
		if (questionCount() > MAX_COUNT || answerCount() > MAX_COUNT)
			throw new PacketException("DNS format: too many resource records count");

		if (additionalCount() > MAX_COUNT || nameServerCount() > MAX_COUNT)
			throw new PacketException("DNS format: too many resource records count");

		readerIndex(HEADER_SIZE);

		// read questions
		for (int i = 0; i < questionCount(); i++) {
			DnsQuestion obj = new DnsQuestion();
			this.getObject(obj);
			questions.add(obj);
		}

		// read answer RRs
		for (int i = 0; i < answerCount(); i++) {
			DnsResourceRecord rr = new DnsResourceRecord(this);
			this.getObject(rr);
			RRs.add(rr);
		}

		// read name server RRs
		for (int i = 0; i < nameServerCount(); i++) {
			DnsResourceRecord rr = new DnsResourceRecord(this);
			this.getObject(rr);
			RRs.add(rr);
		}

		// read additional RRs
		for (int i = 0; i < additionalCount(); i++) {
			DnsResourceRecord rr = new DnsResourceRecord(this);
			this.getObject(rr);
			RRs.add(rr);
		}
		
	}

	public void buildMessage() {
		questionCount(questions.size());
		answerCount(RRs.size());
		additionalCount(0);
		nameServerCount(0);

		writerIndex(HEADER_SIZE);

		// write questions
		for (int i = 0; i < questions.size(); i++) {
			this.putObject(questions.get(i));
		}

		// write RRs
		for (int i = 0; i < RRs.size(); i++) {
			DnsResourceRecord rr = RRs.get(i);
			this.putObject(rr);
		}
	}
	
	/** Get the value of id */
	public int id() {
		return getUInt16(OFFSET_ID);
	}

	/** Set the value of id */
	public DnsPacket id(int value) {
		putUInt16(OFFSET_ID, value);
		return this;
	}

	/** Get the value of qr */
	public int qr() {
		return getUInt1(OFFSET_QR, 7);
	}

	/** Set the value of qr */
	public DnsPacket qr(int value) {
		putUInt1(OFFSET_QR, 7, value);
		return this;
	}

	/** Get the value of op code */
	public int opCode() {
		return getUInt4(OFFSET_OP_CODE, 6);
	}

	/** Set the value of op code */
	public DnsPacket opCode(int value) {
		putUInt4(OFFSET_OP_CODE, 6, value);
		return this;
	}

	/** Get the value of aa */
	public int aa() {
		return getUInt1(OFFSET_AA, 2);
	}

	/** Set the value of aa */
	public DnsPacket aa(int value) {
		putUInt1(OFFSET_AA, 2, value);
		return this;
	}

	/** Get the value of tc */
	public int tc() {
		return getUInt1(OFFSET_TC, 1);
	}

	/** Set the value of tc */
	public DnsPacket tc(int value) {
		putUInt1(OFFSET_TC, 1, value);
		return this;
	}

	/** Get the value of rd */
	public int rd() {
		return getUInt1(OFFSET_RD, 0);
	}

	/** Set the value of rd */
	public DnsPacket rd(int value) {
		putUInt1(OFFSET_RD, 0, value);
		return this;
	}

	/** Get the value of ra */
	public int ra() {
		return getUInt1(OFFSET_RA, 7);
	}

	/** Set the value of ra */
	public DnsPacket ra(int value) {
		putUInt1(OFFSET_RA, 7, value);
		return this;
	}

	/** Get the value of z1 */
	public int z1() {
		return getUInt1(OFFSET_Z1, 6);
	}

	/** Set the value of z1 */
	public DnsPacket z1(int value) {
		putUInt1(OFFSET_Z1, 6, value);
		return this;
	}

	/** Get the value of z2 */
	public int z2() {
		return getUInt2(OFFSET_Z2, 5);
	}

	/** Set the value of z2 */
	public DnsPacket z2(int value) {
		putUInt2(OFFSET_Z2, 5, value);
		return this;
	}

	/** Get the value of Response code */
	public int rcode() {
		return getUInt4(OFFSET_RCODE, 3);
	}

	/** Set the value of Response code */
	public DnsPacket rcode(int value) {
		putUInt4(OFFSET_RCODE, 3, value);
		return this;
	}

	/** Get the value of question count */
	public int questionCount() {
		return getUInt16(OFFSET_QD_COUNT);
	}

	/** Set the value of question count */
	public DnsPacket questionCount(int value) {
		putUInt16(OFFSET_QD_COUNT, value);
		return this;
	}

	/** Get the value of answer records count */
	public int answerCount() {
		return getUInt16(OFFSET_AN_COUNT);
	}

	/** Set the value of answer records count */
	public DnsPacket answerCount(int value) {
		putUInt16(OFFSET_AN_COUNT, value);
		return this;
	}

	/** Get the value of name server records count */
	public int nameServerCount() {
		return getUInt16(OFFSET_NS_COUNT);
	}

	/** Set the value of name server records count */
	public DnsPacket nameServerCount(int value) {
		putUInt16(OFFSET_NS_COUNT, value);
		return this;
	}

	/** Get the value of additional records count */
	public int additionalCount() {
		return getUInt16(OFFSET_QR_COUNT);
	}

	/** Set the value of additional records count */
	public DnsPacket additionalCount(int value) {
		putUInt16(OFFSET_QR_COUNT, value);
		return this;
	}
	
	/** check whether this is a DNS request message */
	public boolean isRequest() {
		return qr() == 1;
	}
	
	/** check whether this is a DNS reply/response message */
	public boolean isReply() {
		return qr() == 1;
	}
	
	/** set to Response mode */
	public void setResponse(boolean value) {
		qr(value ? 1 : 0); // 0 for query, 1 for response
		rd(1); // 1 for Recursion Desired
		aa(value ? 1 : 0); // Authoritative Answer
	}
	
	/** get InetAddress at specified index */
	public InetAddress getAddress(int index) {
		if (index >= 0 && index < addresses.size())
			return addresses.get(index);
		return null;
	}

	/** get cname at specified index */
	public String getCName(int index) {
		if (index >= 0 && index < cnames.size())
			return cnames.get(index);
		return null;
	}

	/** get InetAddress */
	public InetAddress getAddress(String domainName, boolean isIpV6) {
		// if address exists
		if (addresses.size() > 0) {
			return addresses.get(0); // return the first IP address
		} else if (cnames.size() > 0) { // if cname exists
			// get the IP of first cname
			String cname = cnames.get(0);
			return getAddress(cname, isIpV6);
		}
		return null;
	}
	
	public String toString() {
		boolean isNetBIOS = this instanceof NetBIOS;
		
		StringBuilder sb = new StringBuilder();
		sb.append(getClassName());

		String type = PacketUtil.padding(queryTypeName() + "(" + queryType() +")", 10);
		String srcIpStr = PacketUtil.padding(srcIp() == null ? "null" : srcIp().getHostAddress(), 16);
		String dstIpStr = PacketUtil.padding(dstIp() == null ? "null" : dstIp().getHostAddress(), 16);
		sb.append(", Src:").append(srcIpStr);
		sb.append(", Dst:").append(dstIpStr);
		String action = isReply() ? ", Answer " : ", Query  ";
		sb.append(action);
		
		if (isNetBIOS && isReply()) {
			if (queryType() != 0) {
				sb.append(type).append(" ");
			}
		} else {
			sb.append(type).append(" ");
			sb.append(queryName()).append(" ");
		}		
				
		if (isReply()) {
			if (!isNetBIOS) {
				// add DNS response address
				if (getAddress(0) != null) {
					sb.append(getAddress(0).getHostAddress());
				} else if (getCName(0) != null) { 
					sb.append(getCName(0));
				} else {
					sb.append("none");
				}				
			} else {
				// add NetBIOS reply content
				sb.append(((NetBIOS)this).content());
				
			}
		}

		return sb.toString();
	}

	/**
	 * create a response message for A Query
	 * 
	 * @param name domain name
	 * @param addr InetAddress
	 * @param ttl  TTL
	 * @return
	 */
	public DnsPacket responseA(String name, InetAddress addr, long ttl) {
		DnsPacket ret = new DnsPacket();
		ret.capacity(1024);
		ret.id(this.id());
		ret.opCode(this.opCode());
		ret.qr(this.qr());
		ret.aa(this.aa());
		ret.tc(this.tc());
		ret.rd(this.rd());
		ret.ra(this.ra());
		ret.rcode(this.rcode());
		ret.setResponse(true);

		int type = PacketUtil.isIpV6(addr) ? DnsQueryType.AAAA.getValue() : DnsQueryType.A.getValue();

		DnsQuestion q = new DnsQuestion(name, type, DnsPacket.CLASS_INTERNET);
		ret.questions.add(q);
		ret.RRs.add(DnsResourceRecord.answerIp(type, name, addr, ttl));

		ret.buildMessage();
		return ret;
	}

	/**
	 * create a response message for PTR Query
	 * 
	 * @param name
	 * @param ptr
	 * @param ttl
	 * @return
	 */
	public DnsPacket responsePTR(String name, String ptr, long ttl) {
		DnsPacket ret = new DnsPacket();
		ret.capacity(1024);
		ret.id(this.id());
		ret.opCode(this.opCode());
		ret.qr(this.qr());
		ret.aa(this.aa());
		ret.tc(this.tc());
		ret.rd(this.rd());
		ret.ra(this.ra());
		ret.rcode(this.rcode());
		ret.setResponse(true);

		DnsQuestion q = new DnsQuestion(name, queryType(), DnsPacket.CLASS_INTERNET);
		ret.questions.add(q);
		ret.RRs.add(DnsResourceRecord.answerPtr(name, ptr, ttl));

		ret.buildMessage();
		return ret;
	}

	/**
	 * create a response message with error code
	 * 
	 * @param errCode
	 * @return
	 */
	public DnsPacket responseError(int errorCode) {
		DnsPacket ret = new DnsPacket();
		ret.capacity(1024);
		ret.id(this.id());
		ret.opCode(this.opCode());
		ret.qr(this.qr());
		ret.aa(this.aa());
		ret.tc(this.tc());
		ret.rd(this.rd());
		ret.ra(this.ra());		
		ret.setResponse(true);
		ret.rcode(errorCode);  //ret.errorCode = errorCode;

		DnsQuestion q = new DnsQuestion(queryName(), queryType(), DnsPacket.CLASS_INTERNET);
		ret.questions.add(q);
		ret.buildMessage();
		return ret;
	}
	
	/**
	 * Question in DNS message
	 */
	public static class DnsQuestion implements IReadWriteByteArray {

		private DnsName mname = new DnsName("");
		
		/** query name */
		public String name;

		/** query type */
		public int type;

		/** query class */
		public int clazz;

		/** constructor */
		public DnsQuestion() {
		}

		/**
		 * constructor
		 * 
		 * @param name    query name
		 * @param readWriteType    query type
		 * @param clazz   query class
		 */
		public DnsQuestion(String name, int queryType, int queryClass) {
			this.name = name;
			this.type = queryType;
			this.clazz = queryClass;
		}

		public int readByteArray(ByteArray b, int bitOffset) {
			int index = b.readerIndex();
			if (mname == null)  mname = new DnsName("");
			mname.readByteArray(b);  //b.getObject(mname);
			name = mname.name;
			
			type = b.getUInt16();
			clazz = b.getUInt16();
			
			return (b.readerIndex() - index) * 8;
		}

		public int writeByteArray(ByteArray b, int bitOffset) {
			int index = b.writerIndex();
			
			if (mname == null)  mname = new DnsName("");
			mname.name = name;
			mname.writeByteArray(b); //b.putObject(mname);
			
			b.putUInt16(type);
			b.putUInt16(clazz);
			
			return (b.writerIndex() - index) * 8;

		}


	}

	
	/**
	 * Name in DNS message
	 */
	public static class DnsName implements IReadWriteByteArray  {

		public String name = "";
		
		public DnsName() {
			this("");
		}
		
		public DnsName(String name) {
			this.name = name;
		}
		
		/**
		 * read a Name at specified index, its length is less than max bytes
		 * 
		 * @param index      read position
		 * @param maxBytes   max length in bytes
		 * @param depth      loop depth
		 * @return return NameBytes object
		 */
		protected NameBytes readName(ByteArray b, int index, int maxBytes, int depth) {
			if (depth < 0 || depth > 3) // in case of dead loop
				throw new PacketException("invalid name depth");

			String name = "";
			int segCount = 0;
			int readBytes = 0;

			int length = b.getUInt8(index + readBytes);
			readBytes += 1;

			while (length != 0 && (maxBytes <= 0 || readBytes < maxBytes)) {
				if (segCount > 0)
					name += ".";

				if ((length & 0xC0) == 0xC0) { // it is a PTR
					// calculate new_offset
					int b1 = length & 0x3F;
					int b2 = (b.getByte(index + readBytes) & 0xFF);
					readBytes += 1;
					int new_offset = (b1 << 8) | b2;

					// read name from new_offset
					NameBytes p = readName(b, new_offset, maxBytes, depth + 1);
					name += p.name;
					segCount += 1;
					break;

				} else {
					// read string
					name += b.getStringLen(index + readBytes, length);
					readBytes += length;
					segCount += 1;
				}

				length = b.getUInt8(index + readBytes);
				readBytes += 1;
			}

			NameBytes ret = new NameBytes(name, readBytes);
			return ret;
		}
	 
		/**
		 * write a Name at specified index
		 * 
		 * @param index    write position
		 * @param name     name string
		 * @return return count of bytes written
		 */
		protected int writeName(ByteArray b, int index, String name) {
			int oldIndex = index;
			
			if (name != null && name.length() > 0) {
				String[] words = name.split("\\.");

				for (int i = 0; i < words.length; i++) {
					String word = words[i];
					if (word.length() > 255)
						throw new PacketException("name larger than 255");
					b.putUInt8(index, word.length());
					index++;
					index += b.putStringLen(index, word);
				}
			}

			b.putByte(index, (byte) (0));
			return index - oldIndex + 1;
		}

		@Override
		public int readByteArray(ByteArray b, int bitOffset) {
			int index = b.readerIndex();
			NameBytes nb = readName(b, index, 0, 0);
			this.name = nb.name;
			b.readerIndex(index + nb.readBytes);
			return (b.readerIndex() - index) * 8;
		}

		@Override
		public int writeByteArray(ByteArray b, int bitOffset) {
			int index = b.writerIndex();
			int len = writeName(b, index, name);
			b.writerIndex(index + len);
			return len * 8;
		}
			
		/** Result for readName() */
		public static class NameBytes {
			public String name;

			public int readBytes = 0;

			public NameBytes(String name, int readBytes) {
				this.name = name;
				this.readBytes = readBytes;
			}

		}

	}

	/**
	 * ResourceRecord(RR) in DNS message 
	 * 
	   <br><br>
	   <a href="https://www.rfc-editor.org/rfc/rfc1035#section-4.1.3">Resource record format</a><br>
	 <pre>
	The answer, authority, and additional sections all share the same
	format: a variable number of resource records, where the number of
	records is specified in the corresponding count field in the header.
	Each resource record has the following format:
	                                    
	      0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
	    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	    |                                               |
	    /                                               /
	    /                      NAME                     /
	    |                                               |
	    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	    |                      TYPE                     |
	    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	    |                     CLASS                     |
	    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	    |                      TTL                      |
	    |                                               |
	    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	    |                   RDLENGTH                    |
	    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--|
	    /                     RDATA                     /
	    /                                               /
	    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+

	where:

	NAME            a domain name to which this resource record pertains.
	TYPE            two bytes containing one of the RR type (meaning of the data in the RDATA)
	CLASS           two bytes which specify the class of the data in the RDATA field.
	TTL             a 32 bit unsigned integer that specifies the time interval (in seconds).
	RDLENGTH        an unsigned 16 bit integer that specifies the length in bytes of the RDATA field.
	RDATA           a variable length string of bytes that describes the resource.  
	                The format of this information varies  according to the TYPE and CLASS of the 
	                resource record.
	                For example, the if the TYPE is A and the CLASS is IN,
	                the RDATA field is a 4 bytes ARPA Internet address.
	 </pre>
	 *
	 */
	public static class DnsResourceRecord implements IReadWriteByteArray {

		public DnsName mname;
		
		/** name */
		public String name;
		
		/** query type */
		public int type;
		
		/** query class */
		public int clazz;
		
		/** TTL */
		public long ttl;	
		
		/** resource data */
		public byte[] rdata = new byte[0];
		
		private DnsPacket msg = null;

		public DnsResourceRecord() {
			
		}
		
		/** constructor */
		public DnsResourceRecord(DnsPacket msg) {
			this.msg = msg;
		}

		@Override
		public int readByteArray(ByteArray b, int bitOffset) {
			int pos = b.readerIndex();
			
			if (mname == null) mname = new DnsName("");
			mname.readByteArray(b); ////	b.getObject(mname);
			name = mname.name;
			
			type = b.getUInt16();
			if (type > 0xFF)
				throw new PacketException("DNS format: invalid RR type");

			clazz = b.getUInt16();
		
			ttl = b.getUInt32();

			int length = b.getUInt16();
			if (length < 0 || length > 2048)
				throw new PacketException("DNS format: invalid RDATA length of resource record");
			
			DnsQueryType qType = DnsQueryType.of(type);
			switch (qType) {
			case A:
			case AAAA:
				rdata = new byte[length];
				b.get(rdata); //// b.getBytes(rdata);
				try {
					InetAddress addr = InetAddress.getByAddress(this.rdata);
					if (msg != null) msg.addresses.add(addr);
				} catch (UnknownHostException e) {
				}
				break;
			case CNAME:
				mname.readByteArray(b);  ////b.getObject(mname);
				String cname = mname.name;
				if (msg != null) msg.cnames.add(cname);
				break;
			case NetBIOS:
				rdata = new byte[length];
				b.get(rdata); ////b.getBytes(rdata);
				break;
			case NetBIOS_STAT:
				rdata = new byte[length];
				b.get(rdata); ////b.getBytes(rdata);
				if (msg instanceof NetBIOS) {
					NetBIOS nb = (NetBIOS)msg;
					nb.parseRData(rdata);
				}
				break;
			default:
				rdata = new byte[length];
				b.get(rdata); ////b.getBytes(rdata);
				break;
			}

			return (b.readerIndex() - pos) * 8;
		}

		@Override
		public int writeByteArray(ByteArray b, int bitOffset) {
			int pos = b.writerIndex();
			
			if (mname == null) mname = new DnsName("");
			mname.name = name;
			
			mname.writeByteArray(b);////b.putObject(mname);
			b.putUInt16(type);
			b.putUInt16(clazz);
			b.putUInt32(ttl);
			b.putUInt16(rdata.length);
			b.put(rdata); ////b.putBytes(rdata);
			
			return (b.writerIndex() - pos) * 8;
		}

		public static DnsResourceRecord answerIp(int queryType, String name, 
				InetAddress address, long ttl) {
			DnsResourceRecord rr = new DnsResourceRecord();
			rr.name = name;
			rr.clazz = DnsPacket.CLASS_INTERNET;
			rr.type = queryType;
			rr.ttl = ttl;
			rr.rdata = address.getAddress();
			return rr;
		}
		
		/** 
		 * create answer for a PTR query 
		 * 
		 * @param name  domain name
		 * @param ptr   PTR name
		 * @param ttl   Time To Live
		 * @return a new DNSResourceRecord object
		 */
		public static DnsResourceRecord answerPtr(String name, String ptr, long ttl) {
			DnsResourceRecord r = new DnsResourceRecord();
			r.name = name;
			r.type = DnsQueryType.PTR.getValue();
			r.clazz = DnsPacket.CLASS_INTERNET;
			r.ttl = ttl;

			ByteArray b = new ByteArray(255);
			DnsName m =new DnsName(ptr);
			m.writeByteArray(b);////b.putObject(new MessageName(ptr));
			r.rdata = b.getBytes(0, b.writerIndex());
			return r;
		}

		
	}


}