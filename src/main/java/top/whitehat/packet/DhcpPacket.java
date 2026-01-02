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
import java.net.UnknownHostException;

import top.whitehat.NetUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * DHCP protocol
 * 
 * <h3>DHCP message data format</h3>
 * <a href="https://www.rfc-editor.org/rfc/rfc2132"> SEE: RFC 2132</a>
 * 
 * <pre>
 *  
 0               1               2               3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|     op (1)    |    htype (1)  |   hlen (1)    |   hops (1)    |
+---------------+---------------+---------------+---------------+
|                         xid (4)                               |
+-------------------------------+-------------------------------+
|   secs (2)    |   flags (2)   |                               |
+---------------+---------------+---------------+---------------+
|                         ciaddr (4)                            |
+---------------------------------------------------------------+
|                         yiaddr (4)                            |
+---------------------------------------------------------------+
|                         siaddr (4)                            |
+---------------------------------------------------------------+
|                         giaddr (4)                            |
+---------------------------------------------------------------+
|                         chaddr (16)                           |
+---------------------------------------------------------------+
|                         sname (64)                            |
+---------------------------------------------------------------+
|                         file (128)                            |
+---------------------------------------------------------------+
|                       magic cookie (4)                        |
+---------------------------------------------------------------+
|                        options (variable)                     |
+---------------------------------------------------------------+    

Field   Size    Description                         Example Values
​​op​​      1	    Operation Code:1=Request,2=Reply    1 (DHCPDISCOVER)
​​htype   1       Hardware Address Type               1 (Ethernet)
​​hlen​​    1       Hardware Address Length             6 (Mac address)
​​hops​​    1       Hops (incremented by relays)        0 (client)
​​xid​​     4       Transaction ID (random number)      0x2A1B3C4D
​​secs​​    2       Seconds since process started       0
​​flags​​   2       Broadcast/Unicast flag              0x8000 (broadcast)
​​ciaddr​​  4       Client IP Address (if known)        0.0.0.0
​​yiaddr​​  4       Your IP Address(assigned by server) 192.168.1.100
​​siaddr​​  4       Next Server IP Address              192.168.1.1
​​giaddr​​  4       Relay Agent IP Address              0.0.0.0 (no relay)
​​chaddr​​ 16       Client Hardware (Mac) Address       00:1A:2B:3C:4D:5E
​​sname​​  64       Optional Server Hostname            (often empty)
​​file​​  128       Boot Filename                       (often empty)
​​magic   4       Identifies as DHCP(0x63825363)      Fixed value
​​options​​ vars    DHCP Options (TLV format)           Message type, lease time, etc.
 * </pre>
 * 
 * <h3>DHCP Operation Code</h3>
 * <a href="https://www.rfc-editor.org/rfc/rfc2132#section-9.6"> SEE: RFC 2132:
 * Op code</a>
 * 
 * <pre>
			value   Message Type
           -----   ------------
             1     DHCPDISCOVER
             2     DHCPOFFER
             3     DHCPREQUEST
             4     DHCPDECLINE
             5     DHCPACK
             6     DHCPNAK
             7     DHCPRELEASE
             8     DHCPINFORM
 * </pre>
 * 
 * 
 * <h3>DHCP Options</h3>
 * <a href="https://www.rfc-editor.org/rfc/rfc2132#section-3"> SEE: RFC 2132:
 * Extensions</a>
 * 
 * <pre>

Basic format is TLV (Tag-Length-Value) Format

 0               1               2               3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|  Option Code  |  Length (N)   |   Option Data (Value)         |
|     (1 byte)  |   (1 byte)    |         (N bytes)             |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

Option 53: DHCP Message Type - The type of DHCP message (e.g., Discover, Offer, Request, Ack, Nak). 
		   This option is mandatory in all DHCP packets.
Option 12: client information
Option 1: Subnet Mask - Network subnet mask for the client (e.g., 255.255.255.0).
Option 3: Router(Default Gateway) - A list of available routers or gateways on the client's subnet.
Option 6: DNS - Lists the DNS servers available to the client.
Option 15: Domain Name - The domain name that the client should use for DNS hostname resolution.
Option 44: NetBIOS Name Server (WINS) - Lists the NetBIOS Name Service (NBNS) servers (WINS servers).
Option 46: NetBIOS Node Type - Configures the NetBIOS node type (e.g., B-node, P-node, M-node, H-node) 
		   for name resolution.
Option 51: IP Address Lease Time - The duration (in seconds) for which the IP address is leased to the client.
Option 54: Server Identifier - The IP address of the DHCP server offering the lease. 
		   The client uses this to identify which server's offer it is accepting.
Option 55: Parameter Request List - Sent by the client to request specific configuration parameters from 
		   the server (e.g., the client lists the option codes it wants).
Option 58: Renewal Time Value (T1) - Specifies the time interval (in seconds) after which the client enters
		   the RENEWING state to renew its lease with the original server.
Option 59: Rebinding Time Value (T2) - Specifies the time interval (in seconds) after which the 
		   client enters the REBINDING state to renew its lease with any available server.
Option 61: Client Identifier - A unique identifier used by the client. It can be different from the 
		   hardware (Mac) address.
Option 66: TFTP Server Name - Used for network booting (e.g., PXE), specifies the address or name of 
		   the TFTP server from which to download the boot file.
Option 67: Bootfile Name - Used for network booting, specifies the name of the boot file to be loaded 
		   from the TFTP server.
Option 82: Relay Agent Information - Used by DHCP relay agents to forward information about the client's 
		   originating network to the DHCP server. This helps with policy implementation.
Option 119: Domain Search List - Provides a list of domain names for the client to use when resolving 
		   hostnames, allowing for abbreviated domain name searches.
Option 121: Classless Static Route - Used to provide more flexible static routing information to 
		   the client, supporting classless inter-domain routing (CIDR).
 * 
 * 
 * </pre>
 * 
 * <h3>Example of Key Fields</h3>
 * 
 * <pre>
Message    op    ciaddr     yiaddr         Key Options
​​DISCOVER​​    1    0.0.0.0    0.0.0.0        53=1, 55=Parameter List
​​OFFER​​       2    0.0.0.0    192.168.1.100  53=2, 54=Server IP, 51=Lease Time
​​REQUEST​​     1    0.0.0.0    0.0.0.0        53=3, 50=Requested IP, 54=Server IP
​​ACK​​         2    0.0.0.0    192.168.1.100  53=5, 54=Server IP, 51=Lease Time
 * </pre>
 *
 */
public class DhcpPacket extends Layer7Packet implements IMessage {

	public final static long MAGIC = 0x63825363;

	public final static int SERVER_PORT = 67;

	public final static int CLIENT_PORT = 68;

	/* OP code: REQUEST (Client to Server) */
	public final static int REQUEST = 1;

	/* OP code: REPLY (Server to Client) */
	public final static int REPLY = 2;

	/** hardware type of Ethernet */
	public final static int HARDWARE_ETHERNET = 1;

	/** length of hardware address for Ethernet MAC */
	public final static int HARDWARE_LEN_MAC = 6;

	public final static int HEADER_SIZE = 240;

	/** Flags Broadcast ("Please broadcast the reply, I don't have an IP yet"). */
	public final static int FLAGS_BROADCAST = 0x8000;

	/** Flags Unicast */
	public final static int FLAGS_UNICAST = 0;

	/** end of options */
	protected final static int END_OF_OPTIONS = 0xFF;

	/**
	 * Option : Option 53: DHCP Message Type - The type of DHCP message (e.g.,
	 * Discover, Offer, Request, Ack, Nak). This option is mandatory in all DHCP
	 * packets.
	 */
	protected final static int OPTION_DHCP_MSG_TYPE = 53;

	/**
	 * Option 1: subnet Mask - Network subnet mask for the client (e.g.,
	 * 255.255.255.0).
	 */
	protected final static int OPTION_SUBNET_MASK = 1;

	/**
	 * Option 3: Router(Default Gateway) - A list of available routers or gateways
	 * on the client's subnet.
	 */
	protected final static int OPTION_ROUTER = 3;

	/** Option 6: DNS - Lists the DNS servers available to the client. */
	protected final static int OPTION_DNS = 6;

	/** Option 12: HOSTNAME - host name of the client. */
	protected final static int OPTION_HOSTNAME = 12;

	/**
	 * Option 15: Domain Name - The domain name that the client should use for DNS
	 * hostname resolution.
	 */
	protected final static int OPTION_DOMAIN_NAME = 15;

	/**
	 * Option 44: NetBIOS Name Server (WINS) - Lists the NetBIOS Name Service (NBNS)
	 * servers (WINS servers).
	 */
	protected final static int OPTION_NETBIOS_NAME_SERVER = 44;

	/**
	 * Option 46: NetBIOS Node Type - Configures the NetBIOS node type (e.g.,
	 * B-node, P-node, M-node, H-node) for name resolution.
	 */
	protected final static int OPTION_NETBIOS_NODE_TYPE = 46;

	/**
	 * Option 51: IP Address Lease Time - The duration (in seconds) for which the IP
	 * address is leased to the client.
	 */
	protected final static int OPTION_IP_LEASE_TIME = 51;

	/**
	 * Option 54: Server Identifier - The IP address of the DHCP server offering the
	 * lease. The client uses this to identify which server's offer it is accepting.
	 */
	protected final static int OPTION_SERVER_ID = 54;

	/**
	 * Option 55: Parameter Request List - Sent by the client to request specific
	 * configuration parameters from the server (e.g., the client lists the option
	 * codes it wants).
	 */
	protected final static int OPTION_PARM_REQUEST = 55;

	/**
	 * Option 58: Renewal Time Value (T1) - Specifies the time interval (in seconds)
	 * after which the client enters the RENEWING state to renew its lease with the
	 * original server.
	 */
	protected final static int OPTION_RENEWAL_TIME = 58;

	/**
	 * Option 59: Rebinding Time Value (T2) - Specifies the time interval (in
	 * seconds) after which the client enters the REBINDING state to renew its lease
	 * with any available server.
	 */
	protected final static int OPTION_REBINDING_TIME = 59;

	/**
	 * Option 61: Client Identifier - A unique identifier used by the client. It can
	 * be different from the hardware (Mac) address.
	 */
	protected final static int OPTION_CLIENT_ID = 61;

	/**
	 * Option 66: TFTP Server Name - Used for network booting (e.g., PXE), specifies
	 * the address or name of the TFTP server from which to download the boot file.
	 */
	protected final static int OPTION_TFTP_SERVER = 66;

	/**
	 * Option 67: Bootfile Name - Used for network booting, specifies the name of
	 * the boot file to be loaded from the TFTP server.
	 */
	protected final static int OPTION_BOOT_FILE_NAME = 67;

	/**
	 * Option 82: Relay Agent Information - Used by DHCP relay agents to forward
	 * information about the client's originating network to the DHCP server. This
	 * helps with policy implementation.
	 */
	protected final static int OPTION_RELAY_AGENT_INFO = 82;

	/**
	 * Option 119: Domain Search List - Provides a list of domain names for the
	 * client to use when resolving hostnames, allowing for abbreviated domain name
	 * searches.
	 */
	protected final static int OPTION_DOMAIN_SEARCH_LIST = 119;

	/**
	 * Option 121: Classless Static Route - Used to provide more flexible static
	 * routing information to the client, supporting classless inter-domain routing
	 * (CIDR).
	 */
	protected final static int OPTION_STATIC_ROUTE = 121;

	/* DHCP Message Type​​ in Option 53 */
	public final static int TYPE_DISCOVER = 1;
	public final static int TYPE_OFFER = 2;
	public final static int TYPE_REQUEST = 3;
	public final static int TYPE_DECLINE = 4;
	public final static int TYPE_ACK = 5;
	public final static int TYPE_NAK = 6;
	public final static int TYPE_RELEASE = 7;
	public final static int TYPE_INFORM = 8;

	// offset of fields
	private final static int OFFSET_OP = 0;
	private final static int OFFSET_HTYPE = 1;
	private final static int OFFSET_HLEN = 2;
	private final static int OFFSET_HOPS = 3;
	private final static int OFFSET_XID = 4;
	private final static int OFFSET_SECONDS = 8;
	private final static int OFFSET_FLAGS = 10;
	private final static int OFFSET_CLIENT_IP = 12;
	private final static int OFFSET_YOUR_IP = 16;
	private final static int OFFSET_SERVER_IP = 20;
	private final static int OFFSET_RELAY_AGENT_IP = 24;
	private final static int OFFSET_CLIENT_MAC = 28;
	private final static int OFFSET_SERVER_NAME = 44; // optional server hostname(64 byte)
	private final static int OFFSET_FILE_NAME = 108; // Boot filename (e.g., PXE)(128 byte)
	private final static int OFFSET_MAGIC_NUMBER = 236; // Fixed value 0x63825363(signals DHCP options)

	// DHCP message type
	private int _messageType = 0;

	// DHCP option hostName
	private String _hostName = "";

	/** convert a UDPPacket to DNS */
	public static DhcpPacket wrap(UdpPacket udp) {
		DhcpPacket msg = udp.child(DhcpPacket.class);
		return msg;
	}

	/**
	 * create a DHCP request
	 * 
	 * @param clientMac Mac address of the client(who want the new IP)
	 * 
	 * @return DHCP message
	 */
	public static DhcpPacket request(MacAddress clientMac) {
		return request(clientMac, null);
	}

	/**
	 * Create a DHCP request
	 * 
	 * @param clientMac         Mac address of the client(who want the new IP)
	 * @param dhcpServerAddress DHCP server address
	 * 
	 * @return DHCP message
	 */
	public static DhcpPacket request(MacAddress clientMac, Inet4Address dhcpServerAddress) {
		DhcpPacket msg = new DhcpPacket();
		msg.init(HEADER_SIZE + 12);
		msg.op(REQUEST);
		msg.clientMac(clientMac);
		msg.serverIp(dhcpServerAddress != null ? dhcpServerAddress : PacketUtil.anyInet4Address);

		// write options
		msg.writerIndex(HEADER_SIZE);
		// DHCP Discover options(53=Discover, {1})
		msg.putTLV(OPTION_DHCP_MSG_TYPE, new byte[] { DhcpPacket.TYPE_DISCOVER });
		// DHCP Discover options(55=Parameter Request List)
		// Param Req: Subnet, Router, DNS, Domain, MTU, NTP = {1, 3, 6, 15, 28, 42}
		msg.putTLV(OPTION_PARM_REQUEST, new byte[] { 1, 3, 6, 5, 2, 8 });
		msg.putUInt8(END_OF_OPTIONS);

		byte[] data = msg.array(); // msg.getBytes(0, msg.limit());
		UdpPacket udp = UdpPacket.create(PacketUtil.anyInet4Address, CLIENT_PORT, PacketUtil.anyInet4Address,
				SERVER_PORT, data);
		return DhcpPacket.wrap(udp);
	}

	/**
	 * Create DHCP reply message
	 * 
	 * @param req
	 * @return
	 */
	public static DhcpPacket reply(DhcpPacket req, Inet4Address yourIp, Inet4Address serverIp, Inet4Address mask) {
		DhcpPacket msg = new DhcpPacket();
		msg.init(HEADER_SIZE + 12);
		msg.op(REPLY);
		msg.xid(req.xid());
		msg.yourIp(yourIp);
		msg.clientMac(req.clientMac());
		msg.serverIp(serverIp);

		// write options
		msg.writerIndex(HEADER_SIZE);
		msg.putTLV(OPTION_DHCP_MSG_TYPE, new byte[] { DhcpPacket.TYPE_ACK }); // TYPE_OFFSER
		msg.putTLV(DhcpPacket.OPTION_SERVER_ID, serverIp.getAddress());
		msg.putUInt8(END_OF_OPTIONS); // End of options

//	    // 基本选项
//	    List<byte[]> opts = new ArrayList<>();
//	    opts.add(opt(53, new byte[]{(byte)(isAck ? 5 : 2)})); // DHCP Message Type: ACK/Offer
//	    opts.add(opt(54, yourIp.getAddress()));              // Server Identifier
//	    opts.add(opt(1,  ip("255.255.255.0")));            // Subnet Mask
//	    opts.add(opt(3,  ip("192.168.1.1")));            // Router
//	    opts.add(opt(6,  ip("8.8.8.8")));                // DNS
//	    opts.add(opt(51, int32(3600)));                    // Lease Time
//	    opts.add(opt(58, int32(1800)));                    // Renewal (T1)
//	    opts.add(opt(59, int32(3150)));                    // Rebinding (T2)
//
//	    // Request 专用
//	    if (requestedIp != null) {
//	      opts.add(opt(50, requestedIp.getAddress()));
//	    }

		byte[] data = msg.getBytes(0, msg.writerIndex());
		UdpPacket udp = UdpPacket.create(serverIp, SERVER_PORT, PacketUtil.anyInet4Address, CLIENT_PORT, data);
		return DhcpPacket.wrap(udp);
	}
	
	/**
	 * Create DHCP NAK message
	 * 
	 * @param req
	 * @return
	 */
	public static DhcpPacket nak(DhcpPacket req) {
		DhcpPacket msg = new DhcpPacket();
		msg.init(HEADER_SIZE + 12);
		msg.op(REPLY);
		msg.xid(req.xid());
		msg.clientIp(NetUtil.ANY_IP);
		msg.yourIp(NetUtil.ANY_IP);
		msg.serverIp(req.serverIp());
		msg.clientMac(req.clientMac());
		
		// write options
		msg.writerIndex(HEADER_SIZE);
		msg.putTLV(OPTION_DHCP_MSG_TYPE, new byte[] { DhcpPacket.TYPE_NAK }); // TYPE_OFFSER
		msg.putTLV(DhcpPacket.OPTION_SERVER_ID, req.serverIp().getAddress());
		msg.putUInt8(END_OF_OPTIONS); // End of options
		
		byte[] data = msg.getBytes(0, msg.writerIndex());
		UdpPacket udp = UdpPacket.create(req.serverIp(), SERVER_PORT, PacketUtil.anyInet4Address, CLIENT_PORT, data);
		return DhcpPacket.wrap(udp);
		
	}

	public DhcpPacket() {
		super();
	}

	public DhcpPacket(byte[] data) {
		super(data);
		parseMessage();
	}

	/** get message type */
	public int messageType() {
		return _messageType;
	}

	/** set message type */
	public DhcpPacket messageType(int msgType) {
		_messageType = msgType;
		return this;
	}

	/** get message type name */
	public String messageTypeString() {
		switch (_messageType) {
		case 1:
			return "DHCP_DISCOVER";
		case 2:
			return "DHCP_OFFER";
		case 3:
			return "DHCP_REQUEST";
		case 4:
			return "DHCP_DECLINE";
		case 5:
			return "DHCP_ACK";
		case 6:
			return "DHCP_NAK";
		case 7:
			return "DHCP_RELEASE";
		case 8:
			return "DHCP_INFORM";
		default:
			return "";
		}
	}

	/** get Operation Code */
	public int op() {
		return getUInt8(OFFSET_OP);
	}

	/** set Operation Code */
	public DhcpPacket op(int t) {
		putUInt8(OFFSET_OP, t);
		return this;
	}

	public boolean isReply() {
		return op() == REPLY;
	}

	public boolean isRequest() {
		return op() == REQUEST;
	}

	/** get hardware type (value 1 for Ethernet) */
	public int hardwareType() {
		return getUInt8(OFFSET_HTYPE);
	}

	/** set hardware type (value 1 for Ethernet) */
	public DhcpPacket hardwareType(int t) {
		putUInt8(OFFSET_HTYPE, t);
		return this;
	}

	/** get hardware length (value 6 for Mac address) */
	public int hardwareLength() {
		return getUInt8(OFFSET_HLEN);
	}

	/** set hardware length (value 6 for Mac address) */
	public DhcpPacket hardwareLength(int t) {
		putUInt8(OFFSET_HLEN, t);
		return this;
	}

	/** get Hops (incremented by relays) */
	public int hops() {
		return getUInt8(OFFSET_HOPS);
	}

	/** set Hops (incremented by relays) */
	public DhcpPacket hops(int t) {
		putUInt8(OFFSET_HOPS, t);
		return this;
	}

	/** get Transaction ID */
	public long xid() {
		return getUInt32(OFFSET_XID);
	}

	/** set Transaction ID */
	public DhcpPacket xid(long t) {
		putUInt32(OFFSET_XID, t);
		return this;
	}

	/** get Seconds since process started */
	public int seconds() {
		return getUInt8(OFFSET_SECONDS);
	}

	/** set Seconds since process started */
	public DhcpPacket seconds(int t) {
		putUInt8(OFFSET_SECONDS, t);
		return this;
	}

	/** get flags, 0x8000 (broadcast), ?(Unicast) */
	public int flags() {
		return getUInt8(OFFSET_FLAGS);
	}

	/** set flags */
	public DhcpPacket flags(int t) {
		putUInt8(OFFSET_FLAGS, t);
		return this;
	}

	/** get client IP Address (if known) */
	public Inet4Address clientIp() {
		byte[] bs = getBytes(OFFSET_CLIENT_IP, 4);
		try {
			return (Inet4Address) Inet4Address.getByAddress(bs);
		} catch (UnknownHostException e) {
			return PacketUtil.anyInet4Address;
		}
	}

	/** set Client IP Address */
	public DhcpPacket clientIp(Inet4Address addr) {
		put(OFFSET_CLIENT_IP, addr.getAddress());
		return this;
	}

	/** get Your IP Address (assigned by server) */
	public Inet4Address yourIp() {
		byte[] bs = getBytes(OFFSET_YOUR_IP, 4);
		try {
			return (Inet4Address) Inet4Address.getByAddress(bs);
		} catch (UnknownHostException e) {
			return PacketUtil.anyInet4Address;
		}
	}

	/** set Your IP Address (assigned by server) */
	public DhcpPacket yourIp(Inet4Address addr) {
		put(OFFSET_YOUR_IP, addr.getAddress());
		return this;
	}

	/** get Next Server IP Address */
	public Inet4Address serverIp() {
		byte[] bs = getBytes(OFFSET_SERVER_IP, 4);
		try {
			return (Inet4Address) Inet4Address.getByAddress(bs);
		} catch (UnknownHostException e) {
			return PacketUtil.anyInet4Address;
		}
	}

	/** set Next Server IP Address */
	public DhcpPacket serverIp(Inet4Address addr) {
		put(OFFSET_SERVER_IP, addr.getAddress());
		return this;
	}

	/** get Relay Agent IP Address */
	public Inet4Address relayAgentIp() {
		byte[] bs = getBytes(OFFSET_RELAY_AGENT_IP, 4);
		try {
			return (Inet4Address) Inet4Address.getByAddress(bs);
		} catch (UnknownHostException e) {
			return PacketUtil.anyInet4Address;
		}
	}

	/** set Relay Agent IP Address */
	public DhcpPacket relayAgentIp(Inet4Address addr) {
		put(OFFSET_RELAY_AGENT_IP, addr.getAddress());
		return this;
	}

	/** get client MAC address */
	public MacAddress clientMac() {
		byte[] bs = getBytes(OFFSET_CLIENT_MAC, 6);
		return new MacAddress(bs);
	}

	/** set client MAC address */
	public DhcpPacket clientMac(MacAddress m) {
		put(OFFSET_CLIENT_MAC, m.array());
		return this;
	}

	/** get ​magic cookie​ (Fixed value=0x63825363) */
	public long magic() {
		return getUInt32(OFFSET_MAGIC_NUMBER);
	}

	/** set ​magic cookie​ (Fixed value=0x63825363) */
	public DhcpPacket magic(long number) {
		putUInt32(OFFSET_MAGIC_NUMBER, MAGIC);
		return this;
	}

	/** get the DHCP server's host name */
	public String serverName() {
		return getString(OFFSET_SERVER_NAME, UTF_8);
	}

	/** set ​the DHCP server's host name */
	public DhcpPacket serverName(String name) {
		putString(OFFSET_SERVER_NAME, name, UTF_8);
		return this;
	}

	/** get Boot File Name */
	public String bootFilename() {
		return getString(OFFSET_FILE_NAME, UTF_8);
	}

	/** set ​Boot File Name */
	public DhcpPacket bootFilename(String name) {
		putString(OFFSET_FILE_NAME, name, UTF_8);
		return this;
	}

	/** get host name */
	public String hostName() {
		return _hostName;
	}

	/** parse message */
	@Override
	public void parseMessage() {
		// parse DHCP options(start from offset 240)
		int offset = HEADER_SIZE;
		int totalLength = capacity();
		while (offset < totalLength) {
			int code = getUInt8(offset);

			if (offset + 1 > totalLength)
				break; // exceed borders

			if (code == 0) { // Padding
				offset++;
				continue;
			}

			if (code == END_OF_OPTIONS) { // End of options (255)
				break;
			}

			int len = getUInt8(offset + 1);
			if (offset + 2 + len > totalLength) {
				break; // data exceed borders
			}

			// whether it is DHCP message type(code = 53)
			if (code == OPTION_DHCP_MSG_TYPE && len == 1) {
				_messageType = getUInt8(offset + 2); // DHCP Message Type
			}

			// when it is DHCP OPTION_CLIENT_ID(code = 61)
			if (code == OPTION_HOSTNAME) {
				_hostName = this.getStringLen(offset + 2, len);
			}

			// when it is DHCP OPTION_CLIENT_ID(code = 61)
			if (code == OPTION_CLIENT_ID) {
				// first byte is data type
				int type = getByte(offset + 2);
				// process by type
				switch (type) {
				case 0x01: // MAC address
					// get data
					byte[] data = new byte[len - 1];
					this.get(offset + 3, data);
					// System.out.println("Client MAC:" + new MacAddress(data));
					break;
				case 0x02: // Hardware defined in RFC 1700
				case 0xff: // IAID
					break;
				}
			}

			// goto next option
			offset += 2 + len;
		}
	}

	@Override
	public void buildMessage() {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClassName());

		if (op() == REQUEST) {
			sb.append(", DHCP Request");
			sb.append(", Server IP: ").append(serverIp().getHostAddress());
			sb.append(", Client MAC: ").append(clientMac());
			sb.append(", Client IP: ").append(clientIp().getHostAddress());
		} else if (op() == REPLY) {
			sb.append(", DHCP Reply  ");
			sb.append(", Server IP: ").append(serverIp().getHostAddress());
			sb.append(", Client MAC: ").append(clientMac());
			sb.append(", Your IP: ").append(yourIp().getHostAddress());
		} else {
			sb.append(", DHCP opCode: ").append(op());
			sb.append(", Client MAC: ").append(clientMac());
			sb.append(", Your IP: ").append(yourIp().getHostAddress());
			sb.append(", Server IP: ").append(serverIp().getHostAddress());
		}

		if (_hostName.length() > 0)
			sb.append(", Host Name: ").append(hostName());

		return sb.toString();
	}

	public DhcpPacket init(int size) {
		capacity(size);
		magic(MAGIC);
		hardwareType(HARDWARE_ETHERNET);
		hardwareLength(HARDWARE_LEN_MAC);
		xid(NetUtil.randomInt());
		flags(FLAGS_BROADCAST);
		return this;
	}
}
