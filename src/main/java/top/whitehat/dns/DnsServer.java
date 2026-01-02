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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import top.whitehat.NetCard;
import top.whitehat.NetUtil;
import top.whitehat.packet.Packet;
import top.whitehat.udp.UdpServer;

/** A DNS server */
public class DnsServer extends UdpServer {
	
	protected static Host LOCAL_HOST = new Host("127.0.0.1");
	
	/** upper DNS server */
	protected UpperDns upperDns = new UpperDns(this);
	
	/**
	 * Name of this DNS server
	 * 
	 * nsServer.address is the InetAddress
	 * nsServer.name is the domain name
	 */
	protected Host nsServer = new Host();
	
	/** records of domain information */
	private DnsRecords records = new DnsRecords();
	
	/** filters  */
	private DnsFilters filters = new DnsFilters();

	/** debug mode: 0(Silent), 1(INFO), 2(WARING), 3(ERROR) */
	private int debug = 1;

	/** local only status: indicates whether accept commands only from local machine */
	private boolean localOnly = true;
	
	/** Constructor: create server on specified NetCard */
	public DnsServer(NetCard netCard) {
		super(netCard);
	}
	
	/** Get debug mode: 0(Silent), 1(INFO), 2(WARING), 3(ERROR) */
	public int getDebug() {
		return this.debug;
	}

	/** Set debug mode: 0(Silent), 1(INFO), 2(WARING), 3(ERROR) */
	public boolean setDebug(int debug) {
		this.debug = debug;
		return true;
	}
	
	/** Get local command status: indicates whether accept commands only from local machine */
	public boolean getLocalOnly() {
		return this.localOnly;
	}

	/** Set local command status: indicates whether accept commands only from local machine */
	public boolean setLocalOnly(boolean localOnly) {
		this.localOnly = localOnly;
		return true;
	}
	
	/** Set upper DNS server */
	public boolean setUpperDnsServer(String ip) {
		upperDns.setServer(ip);
		return true;
	}
	
	/** Get upper DNS server */
	public InetAddress getUpperDnsAddress() {
		return upperDns.getAddress();
	}
	
	/** Set domain name and public ip of this DNS server
	 * 
	 * @param domainName   The domain name of this DNS server, such as: ns.some.com
	 * @param ip           The public IP address of this DNS server
	 */
	public void setNsName(String name, String ip) {
		nsServer.set(name, NetUtil.toInetAddress(ip, null));
	}
	
	
	/** Get DNS records */
	public DnsRecords getRecords() {
		return this.records;
	}

	/** Get DNS filters */
	public DnsFilters getFilters() {
		return this.filters;
	}
	
	/** Add a domain information record */
	public void addRecord(String domainName, String ipAddress, String ipv6Address) {
		getRecords().set(domainName, NetUtil.toInetAddress(ipAddress), NetUtil.toInetAddress(ipv6Address), DnsPacket.DEFAULT_TTL);
	}
	
	/** add a domain information record */
	public void addRecord(String domainName, String ipAddress) {
		addRecord(domainName, ipAddress, null);
	}

	/** add domain information records */
	public void addRecord(List<String> domainNames, String ipAddress) {
		for (String name : domainNames) {
			addRecord(name, ipAddress);
		}
	}

	/** add a filter */
	public void addFilter(String domainName, int filterMode) {
		getFilters().set(domainName, filterMode);
	}
	
	/** add filters */
	public void addFilter(List<String> domainNames, int filterMode) {
		for (String name : domainNames)
			getFilters().set(name, filterMode);
	}
	
	/** set redirect IP address when filter level is Filters.WARING */
	public void setWarningIp(String ip) {
		filters.warningServer.setIp(ip);
	}

	/** set redirect IP address when filter level is Filters.STOP */
	public void setStopIp(String ip) {
		filters.stopServer.setIp(ip);
	}

	/** set redirect IP address when filter level is Filters.WARTRACEING */
	public void setTraceIp(String ip) {
		filters.traceServer.setIp(ip);
	}
	
	/** print debug information */
	protected void debug(Object... args) {
		if (this.debug >= 2)
			NetUtil.println(args);
	}

	private InetAddress _myIp = null;
	private InetAddress myIp() {
		if (_myIp == null) _myIp = netCard.ip();
		return _myIp;
	}
	
	@Override
	protected void receivePacket(Packet packet) {
		try {
			DnsPacket dns = packet.getPacket(DnsPacket.class, DNS.PORT);
			if (dns == null) return;
			
			// if the packet is from uppers DNS, it will be transfer to original client
			if (upperDns.processResponse(dns)) {
				return;
			}
			
			if (!dns.dstIp().equals(myIp())) {
				return;
			}
						
			// create a session
			DnsSession session = new DnsSession(this, dns); 
			
			// run the session
			session.run();
		} catch (Exception e) {
			// handle exception
			if (this.debug >= 2) e.printStackTrace();
		}
	}
	
	
	/** A Host object include name and InetAddress */
	public static class Host {

		private String name = "";

		private InetAddress address = null;

		public Host() {

		}

		public Host(InetAddress address) {
			setAddress(address);
		}

		public  Host(String hostName) {
			setName(hostName);
		}
		
		protected void set(String name, InetAddress addr) {
			this.name = name;
			this.address = addr;
		}

		public boolean isEmpty() {
			return address == null;
		}

		public InetAddress getAddress() {
			if (address == null) {
				try {
					address = InetAddress.getByName(name);
				} catch (UnknownHostException e) {
					address = null;
				}
			}
			return address;
		}

		public InetAddress getAddressByDefault() {
			InetAddress addr = getAddress();
			if (addr == null)
				return LOCAL_HOST.getAddress();
			else
				return addr;
		}

		public void setAddress(InetAddress address) {
			this.address = address;
			this.name = address.getHostName();
		}

		public String getName() {
			return name;
		}

		public String getPtrName() {
			StringBuilder sb = new StringBuilder();
			if (this.address != null) {
				byte[] bytes = this.address.getAddress();
				for (int i = bytes.length - 1; i >= 0; i--) {
					if (i != bytes.length - 1)
						sb.append(".");
					sb.append(String.valueOf((int) (bytes[i] & 0xFF)));
				}
				sb.append(".in-addr.arpa");
			}
			return sb.toString();
		}

		public boolean setName(String hostName) {
			try {
				address = InetAddress.getByName(hostName);
				name = hostName;
				return true;
			} catch (UnknownHostException e) {
				return false;
			}
		}

		public boolean setIp(String ip) {
			try {
				address = InetAddress.getByName(ip);
				if (NetUtil.isIpV4(ip))
					name = ip;
				else
					name = address.getHostName();
				return true;
			} catch (UnknownHostException e) {
				return false;
			}
		}

		public String getIp() {
			if (address == null)
				return "";
			return address.getHostAddress();
		}

	}
	
	/** Record of domain information */
	public static class DnsRecord {

		/** address */
		public InetAddress address;

		/** (Time To Live) */
		public int ttl = 0;

		/** Constructor */
		public DnsRecord(InetAddress address, int ttl) {
			this.address = address;
			this.ttl = ttl;
		}

	}

	/** Records */
	public static class DnsRecords {

		/** a map to hold records */
		private Map<String, DnsRecord> records = new HashMap<String, DnsRecord>();
		
		/** a map to hold ipv6 records */
		private Map<String, DnsRecord> ipv6Records = new HashMap<String, DnsRecord>();
		
		/** Constructor */
		public DnsRecords() {
		}
		
		/** find DNS record of specified domain name
		 * 
		 * @param domainName  The domain name to find
		 * @param isV6        Indicate whether find IPv6 address
		 * 
		 * @return return DNS record object is found, return null if not found.
		 */
		public DnsRecord get(String domainName, boolean isV6) {
			Map<String, DnsRecord> rs = isV6 ? ipv6Records : records;
			
			synchronized (rs ) {
				if (rs.containsKey(domainName)) {
					return rs.get(domainName);
				}
			}
			
			return null;
		}
		
		private InetAddress any6Address = NetUtil.toInetAddress("::0");
		
		/**
		 * Set domain information. If domain not exists, create a new record.
		 * 
		 * @param domainName The domain name
		 * @param ip         The InetAddress
		 * @param ipV6       The IpV6 InetAddress
		 * @param ttl        Time To Live in seconds
		 */
		public void set(String domainName, InetAddress ip, int ttl) {
			set(domainName, ip, any6Address, ttl);
		}
		
		/**
		 * Bind specified domain to specified IPv4, IPV6 address.
		 * 
		 * @param domainName The domain name
		 * @param ipV4       The Inet4Address
		 * @param ipV6       The IpV6 InetAddress
		 * @param ttl        Time To Live in seconds
		 */
		public void set(String domainName, InetAddress ipV4, InetAddress ipV6,  int ttl) {
			synchronized (records) {
				records.put(domainName, new DnsRecord(ipV4, ttl));
			}
			
			if (ipV6 == null) ipV6 = any6Address;
			
			if (ipV6 instanceof Inet6Address) {
				ipv6Records.put(domainName, new DnsRecord(ipV6, ttl));
			} else {
				throw new RuntimeException("not a ipv6 address");
			}
		}

		/**
		 * Bind specified domain to specified IPv4 address.
		 * 
		 * @param domainName  The domain name
		 * @param ipV4          The InetAddress
		 */
		public void set(String domainName, InetAddress ipV4) {
			set(domainName, ipV4, DnsPacket.DEFAULT_TTL);
		}
		
		
		/** Delete record of specified domain */
		public void remove(String domainName) {
			synchronized (records) {
				records.remove(domainName);
			}
		}


	}

	/** Upper DNS server.
	 * 
	 * If a domain is not found in this DNS, then query it to the upper DNS server.
	 */
	public static class UpperDns {
		
		/** Host of upper DNS server */
		private Host upperDnsServer = new Host();

		private DnsServer server = null;
		
		protected static int MAX_ID = 0x3FFFF;
		
		/** last ID that send to upper DNS server */
		private Integer lastID = 0;
		
		private HashMap<Integer, DnsPacket> requests = new HashMap<Integer, DnsPacket>();
		
		public UpperDns(DnsServer server) {
			this.server = server;
		}
		
		public String getServer() {
			return upperDnsServer.getIp();
		}
		
		public void setServer(String ip) {
			upperDnsServer.setIp(ip);
		}
		
		public InetAddress getAddress() {
			return upperDnsServer.getAddress();
		}
		
		public void getAddress(InetAddress address) {
			upperDnsServer.setAddress(address);
		}
	
		public synchronized int getNewId() {
//			synchronized (lastID) {
			lastID += 1;
			if (lastID >= MAX_ID)
				lastID = 1;
			return lastID;
//			}
		}	
		
		/**
		 * Send the response to client
		 * 
		 * @param buffer  response data
		 */
		private void redirectToClient(DnsPacket dns) {
			// get id of this DNS packet
			int thisId = dns.id();
			
			DnsPacket request = null;
			synchronized (requests) {
				// find original request
				request = requests.getOrDefault(thisId, null);
				if (request != null)
					requests.remove(thisId);
			}

			// if found
			if (request != null) {
				int oldId = request.id();
				// change the id to oldId
				dns.id(oldId);
				// send to the original request
				server.send(request.srcIp(), request.srcPort(), dns.getBytes());
				server.debug("transfer the response from upper dns ", dns.srcIpPortStr(), "id=", dns.id());

			}
		}
		
		/**
		 * Send the response to client
		 * 
		 * @param buffer  response data
		 */
		private void redirectToClient(DnsPacket head, byte[] buffer) {
			// 凭ID找到原Session
			int thisId = head.id();
			DnsPacket request = null;
			
			synchronized (requests) {
				request = requests.getOrDefault(thisId, null);
				if (request != null)
					requests.remove(thisId);
			}

			if (request != null) {
				int oldId = request.id();
				// change the first two byte to oldId
				buffer[0] = (byte) ((oldId & 0xFF00) >> 8);
				buffer[1] = (byte) (oldId & 0xFF);

				server.send(request.srcIp(), request.srcPort(), buffer);
			}
		}
		
		public boolean processResponse(DnsPacket dns) {
			// if the packet is a response from upper DNS
			if (dns != null && dns.isReply() && dns.srcIp().equals(upperDnsServer.getAddress())) {
				// redirect the response to original client
				redirectToClient(dns);
				return true;
			}
			
			return false;
		}
		
		/** process the response from upper DNS */
	 	public boolean processResponse(InetSocketAddress address, byte[] buffer) {
			// get DNS message
			DnsPacket dns = new DnsPacket(buffer);
			
			// if the message is response from upper DNS
			if (dns.isReply()) {
				if (address.getAddress().equals(upperDnsServer.getAddress())) {
					server.debug("response from", dns.srcIpPortStr(), "id=", dns.id());
					// redirect the response to original client
					redirectToClient(dns, buffer);
				} 
				return true;
			}	
			
			return false;
		}

		
	 	/** make a query to upper DNS */
		public boolean query(DnsPacket request) {
			try {
				InetAddress addr = upperDnsServer.getAddress();
				if (addr == null)
					return false;

				// the original ID
				int oldId = request.id();			
				// create a new ID that used to communicate with upper DNS
				int newId = getNewId();			
				// change the id to newId
				request.id(newId);
				
				// send data to upper DNS
				InetSocketAddress target = new InetSocketAddress(addr, DNS.PORT);
				server.send(target.getAddress(), target.getPort(), request.getBytes());
				
				// register the relation: newId->request
				requests.put(newId, request);
				// restore old ID
				request.id(oldId); 
				
				server.debug("send to upper dns", addr, "id=", newId);
				return true;

			} catch (Exception e) {
//				server.error(e); //TODO
				return false;
			}		
		}

		
	}

	/** Domain name filter */
	public static class DnsFilters {

		/** filter mode: resolve the domain name normally */
		public final static int PASS = 0;

		/** filter mode: resolve the domain name, trace this action */
		public final static int TRACE = 1;

		/** filter mode: show warning webpage, send this query to warning server */
		public final static int WARNING = 2;

		/** filter mode: show warning webpage, resolve the domain name to local address */
		public final static int STOP = 3;

		/** filter mode: refuse to resolve the domain nam */
		public final static int REJECT = 4;

		/** filter command words */
		private static HashMap<String, Integer> keywords = new HashMap<String, Integer>() {
			private static final long serialVersionUID = 1L;

			{
				put("pass", DnsFilters.PASS);
				put("trace", DnsFilters.TRACE);
				put("warning", DnsFilters.WARNING);
				put("stop", DnsFilters.STOP);
				put("reject", DnsFilters.REJECT);
				put("trace", DnsFilters.TRACE);
			}
		};

		/** Whether the string is a keyword*/
		public static boolean isKeyword(String k) {
			if (k == null)
				return false;
			return keywords.containsKey(k);
		}

		/** get filter mode of the keyword */
		public static int getValue(String k) {
			return keywords.getOrDefault(k, DnsFilters.PASS);
		}

		/** A map to hold domain names */
		private Map<String, Integer> domains = new HashMap<String, Integer>();

		/** Warning server: For domain with Filters.WARNING mode, resolve to this address */
		public Host warningServer = new Host();
		
		/** Trace server: For domain with Filters.TRACE mode,。query is clone to this server */
		public Host traceServer = new Host();
		
		/** Stop server: For domain with Filters.STOP mode, resolve to this address */
		public Host stopServer = new Host();	

		/** Constructor */
		public DnsFilters() {

		}

		/** set filter mode to specified domain.
		 *
		 * @param domain  the domain name
		 * @param mode    the filter mode
		 */
		public void set(String domain, int mode) {
			if (domain == null)
				return;

			if (domain.startsWith("."))
				domain = domain.substring(1);

			domain = domain.toLowerCase().trim();
			if (domain.length() == 0)
				return;

			synchronized (domains) {
				if (mode <= DnsFilters.PASS)
					domains.remove(domain);
				else
					domains.put(domain, mode);
			}
		}

		/** get filter mode of specified domain.
		 * 
		 * @param domain  the domain name
		 * @return
		 */
		public int get(String domain) {
			synchronized (domains) {
				return domains.getOrDefault(domain, DnsFilters.PASS);
			}
		}

		/** analysis the domain, return the filter mode of specified domain.
		 * 
		 * @param domain the domain name
		 * @return
		 */
		public int getFilterMode(String domain) {
			if (domain == null)
				return DnsFilters.STOP;

			domain = domain.toLowerCase().trim();
			if (domain.length() == 0)
				return DnsFilters.STOP;

			// split domain name by dot
			String[] words = domain.split("\\.");
			String name = "";

			// judge whether the subdomain should be filtered
			for (int i = words.length - 1; i >= 0; i--) {
				if (name.length() == 0)
					name = words[i];
				else
					name = words[i] + "." + name;

				int mode = get(name);
				if (mode > DnsFilters.PASS)
					return mode;
			}

			return DnsFilters.PASS;
		}

	}

	
}
