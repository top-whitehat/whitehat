package top.whitehat.dns;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import top.whitehat.packet.MacAddress;
import top.whitehat.packet.UdpPacket;

/**
 * NetBIOS protocol<br>
 * <br>
 * 
 * <a href="https://www.rfc-editor.org/rfc/rfc1002"> SEE: RFC1002</a> <br>
 * https://blog.csdn.net/weixin_39990401/article/details/110884276
 * 
 */
public class NetBIOS extends DnsPacket {
	
	/**
	 * Create an NetBIOS name service query message
	 * 
	 * @param srcIp      Source IP address
	 * @param srcPort    Source port
	 * @param dstIp      Destination IP address
	 * @param id         Message id
	 * @param queryName       The query name
	 * 
	 * @return DnsPacket
	 */
	public static NetBIOS query(InetAddress srcIp, int srcPort, InetAddress dstIp, int id, String queryName) {
		int queryType = DnsQueryType.NetBIOS_STAT.getValue();
		
		NetBIOS msg = new NetBIOS();
		msg.capacity(512);		
		msg.id(id);
		msg.qr(0);  // 0 means query in NetBIOS

		// correct query name when it is empty
		if (queryName == null || queryName.length() == 0)queryName = "*";
		// create encoded query name
		queryName = NetBIOSName.createQueryName(queryName);
		
		// create a DNS question
		DnsQuestion q = new DnsQuestion(queryName, queryType, DnsPacket.CLASS_INTERNET);
		msg.questions.add(q);		
		msg.buildMessage();
		
		// create a UdpPacket whose payload is the msg
		byte[] payload = msg.getBytes(0, msg.writerIndex()); 
		UdpPacket udp = UdpPacket.create(srcIp, srcPort, dstIp, NetBIOS.PORT, payload);
		return udp.child(NetBIOS.class);
	}
	
	/** NetBIOS port */
	public static final int PORT = 137;

	protected static final int OPCODE_QUERY = 0;
	protected static final int OPCODE_REGISTRATION = 5;
	protected static final int OPCODE_RELEASE = 6;
	protected static final int OPCODE_WACK = 7;
	protected static final int OPCODE_REFRESH = 8;

	/** information of NetBIOS */
	protected NetBIOSInfo info = new NetBIOSInfo();

	/** MacAddress in NetBIOS reply */
	protected MacAddress macAddress;

	/** NetBIOS query name */
	protected NetBIOSName netBIOSQueryName;

	/** Create an empty NetBIOS Packet */
	public NetBIOS() {
		super();
	}

	/** Create a NetBIOS Packet from specified data */
	public NetBIOS(byte[] data) {
		super(data);
	}
	
	/** parse message */
	public void parseMessage() {
		// DNS parseMessage()
		super.parseMessage();
		
		// Additional, parse NetBIOS query name
		try {
			String encodedStr = super.queryName();
			netBIOSQueryName = new NetBIOSName(encodedStr);
		} catch (Exception e) {
		}
	}

	/** Return the query domain name (the first question) */
	public String queryName() {
		return netBIOSQueryName == null ? "" : netBIOSQueryName.toString();
	}

	/** Parse RData of the answer RR whose type is NetBIOS_STAT */
	protected void parseRData(byte[] data) {
		int length = data.length;
		int offset = 0;
		if (length < 1 || offset + length > data.length) {
			return;
		}

		ByteBuffer buffer = ByteBuffer.wrap(data, offset, length);

		// read number of names
		int numberOfNames = buffer.get() & 0xFF;

		// read NetBIOS name array (every name is 18 bytes)
		for (int i = 0; i < numberOfNames; i++) {
			if (buffer.remaining() < 18)
				break;

			byte[] nameBytes = new byte[16];
			buffer.get(nameBytes);

			int flags = buffer.getShort() & 0xFFFF;

			NetBIOSName nameInfo = new NetBIOSName();
			String nameStr = new String(nameBytes).trim();
			nameInfo.name = nameStr;
			nameInfo.flags = flags;
			nameInfo.isGroup = (flags & 0x8000) != 0; // 最高位判断是唯一名称还是组名称
			nameInfo.nameType = (flags >> 8) & 0x1F; // 提取名称类型
			if (!nameInfo.isGroup && this.cnames.indexOf(nameStr) < 0) 
				this.cnames.add(nameStr);
			this.info.add(nameInfo);
		}

		// read MAC address(6 bytes) after name array
		if (buffer.remaining() >= 6) {
			byte[] bs = new byte[6];
			buffer.get(bs);
			this.macAddress = new MacAddress(bs);
		}
	}

	/** Return content String for toString() */
	protected String content() {
		StringBuilder sb = new StringBuilder();
		for(NetBIOSName nname : info) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(nname.name);
		}
		return sb.toString();
	}
	
	/** Return MAC address in the NetBIOS reply */
	public MacAddress getMac() {
		return macAddress;
	}

	/** Return List of names in the NetBIOS reply */
	public NetBIOSInfo getInfo() {
		return info;
	}

	/** Return unique name in the NetBIOS reply */
	public String getName() {
		if (this.cnames.size() > 0) {
			return this.cnames.get(0);
		}
		return null;
	}
}
