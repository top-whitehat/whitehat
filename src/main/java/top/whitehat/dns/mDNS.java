package top.whitehat.dns;

import java.net.InetAddress;

import top.whitehat.NetUtil;
import top.whitehat.packet.PacketUtil;
import top.whitehat.packet.UdpPacket;

/** mDNS protocol<br><br>
 * 
 * The Multicast DNS (mDNS)​ protocol is a zero-configuration networking technology that <br>
 * allows devices on the same local network to discover and resolve each other's hostnames<br>
 * into IP addresses without needing a traditional, centralized DNS server. It is <br>
 * designed to work in small networks and uses essentially the same programming <br>
 * interfaces and packet formats as the standard unicast DNS system.
 * 
 * 
 * <h3>Multicast DNS (mDNS)</h3>
 * <a href="https://www.rfc-editor.org/rfc/rfc6762">SEE: RFC6762 mDNS</a>
 * 
 * <h3>DNS-Based Service Discovery (DNS-SD)</h3>
 * <a href="https://www.rfc-editor.org/rfc/rfc6763">SEE: RFC6763 DNS-SD</a>
 * 
 */
public class mDNS extends DnsPacket {
	
	/** port for mDNS protocol */
	public static final int PORT = 5353;
	
	/** mDNS protocol IpV4 server */
	public static final InetAddress LocalServer = PacketUtil.toInetAddress(224, 0, 0, 251); // 224.0.0.251

	/** mDNS protocol IpV6 server */
	public static final InetAddress LocalServerV6 = NetUtil.toInetAddress("ff02::fb");
	
	/**
	 * Create an mDNS query message
	 * 
	 * @param srcIp      Source IP address
	 * @param srcPort    Source port
	 * @param dstIp      Destination IP address, use default server(224.0.0.251) if dstIp is null
	 * @param id         Message id
	 * @param type       Query type
	 * @param domain     The domain name to query
	 * 
	 * @return mDNS
	 */
	public static mDNS query(InetAddress srcIp, int srcPort, InetAddress dstIp, int id, int queryType, String domain) {
		mDNS msg = new mDNS();
		msg.capacity(1024);
		msg.id(id);
		msg.setResponse(false);

		DnsQuestion q = new DnsQuestion(domain, queryType, DnsPacket.CLASS_INTERNET);
		msg.questions.add(q);		
		msg.buildMessage();
	
		byte[] data = msg.getBytes(0, msg.writerIndex());
		if (dstIp == null) dstIp = LocalServer;
		UdpPacket udp = UdpPacket.create(srcIp, srcPort, dstIp, PORT, data);		
		return udp.child(mDNS.class);
	}
	
	
	
//	/**
//	 * Create an request message
//	 * 
//	 * @param srcIp      Source IP address
//	 * @param srcPort    Source port
//	 * @param id     Message id
//	 * @param type   Query type
//	 * @param domain The domain name to query
//	 * 
//	 * @return mDNS
//	 */
//	public static mDNS request(InetAddress srcIp, int srcPort, int id, int queryType, String domain) {
//		return query(srcIp, srcPort, id, queryType, domain);
//	}
//	
	/**
	 * Create an A query message
	 * 
	 * @param type   query type
	 * @param domain domain name
	 * 
	 * @return DnsPacket
	 */
	public static mDNS requestA(InetAddress srcIp, int srcPort, String domain) {
		return query(srcIp, srcPort, null, 1, DnsQueryType.A.getValue(), domain);
	}
	
	
	// _services._dns-sd._udp.local
	// _services._dns-sd._udp.local
		// _airplay._tcp.local , _rdlink._tcp.local, _companion-link._tcp.local,  _homekit._tcp.local 
	
	/**
	 * Create an find-service message
	 * 
	 * @param srcIp        Source IP address
	 * @param srcPort      Source port
	 * @param dstIp        Destination IP address, use default server(224.0.0.251) if dstIp is null
	 * @param serviceName  service name, could be null,  such as: _airplay._tcp.local
	 * 
	 * @return mDNS message
	 */
	public static mDNS findService(InetAddress srcIp, int srcPort, InetAddress dstIp, String serviceName) {
		// set PTR query _services._dns-sd._udp.local, the device will response its service
		if (serviceName == null) serviceName = "_services._dns-sd._udp.local"; 
		return query(srcIp, srcPort, dstIp, 1,  DnsQueryType.PTR.getValue(), serviceName);
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
	public static DnsPacket responseA(mDNS query, String name, InetAddress addr, long ttl) {
		return query.responseA(name, addr, ttl);
	}
	
	/** Create an empty mDNS Packet */
	public mDNS() {
		super();
	}

	/** Create a mDNS Packet from specified data */
	public mDNS(byte[] data) {
		super(data);
	}
	
}
