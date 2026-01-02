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
import java.util.ArrayList;
import java.util.List;

import top.whitehat.udp.UdpClient;

/***
 * DnsClient
 * 
 */
public class DnsClient extends UdpClient {

	
	/** Create a DnsClient object of specified DNS server */
	public static DnsClient of(String dnsServer) {
		return new DnsClient(dnsServer);
	}

	/** timeout in milliseconds: wait time for response of the DNS server */
	private int timeout = 2000;

	/** retry times */
	private int retry = 2;

	/** DNS server */
	private String dnsServer;

	/** transaction id, increase 1 after each query */
	protected int id = 0;

	/** debug level */
	private int debugLevel = 0;

	/** Constructor: create a DnsClient */
	public DnsClient() {
		this(DNS.getDefaultDnsServer());
	}

	/**
	 * Constructor: create a DnsClient with specified DNS server
	 * 
	 * @param dnsServer DNS server ip address or host name
	 */
	public DnsClient(String dnsServer) {
		setDnsServer(dnsServer);
	}

	/** get debug level */
	public int getDebug() {
		return this.debugLevel;
	}

	/** set debug level */
	public DnsClient setDebug(int debug) {
		this.debugLevel = debug;
		return this;
	}

	/** set DNS server */
	public void setDnsServer(String dnsServer) {
		this.dnsServer = dnsServer;
	}

	/** get DNS server */
	public String getDnsServer() {
		return dnsServer;
	}

	/** set retry times */
	public void setRetry(int retry) {
		if (retry < 0)
			retry = 0;
		this.retry = retry;
	}

	/** get retry times */
	public int getRetry() {
		return retry;
	}

	/** set timeout in milliseconds */
	public void setTimeout(int timeout) {
		if (timeout < 0)
			timeout = 0;
		this.timeout = timeout;
	}

	/** get timeout in milliseconds */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * make a DNS query
	 * 
	 * @param queryType  Query type string
	 * @param domainName The domain name, such as "www.some.com"
	 * @return return DNS message object if success, return null if failed.
	 */
	public DnsPacket query(String queryType, String domainName) {
		DnsQueryType type = DnsQueryType.of(queryType);
		if (DnsQueryType.UNKNOWN.equals(type))
			throw new IllegalArgumentException("unknown query type " + queryType);
		return query(type.getValue(), domainName);
	}

	/**
	 * make a DNS query
	 * 
	 * @param queryType  Query type
	 * @param domainName The domain name, such as "www.some.com"
	 * @return return DNS message object if success, return null if failed.
	 */
	public DnsPacket query(int queryType, String domainName) {
		// compose request message
		id = id + 1;
		DnsPacket request = DnsPacket.create(queryType, id, domainName);
		byte[] requestBytes = request.getBytes();
		byte[] responseBytes = null;

		// request, wait for response
		responseBytes = udpQuery(getDnsServer(), requestBytes, requestBytes.length, this.timeout, this.retry);

		// if there is response
		if (responseBytes != null) {
			// parse response message
			DnsPacket response = new DnsPacket(responseBytes);
			return response;
		}

		return null;
	}

	/**
	 * make a DNS query, get the ip addresses of specified domain name
	 * 
	 * @param domainName The domain name, such as "www.some.com"
	 * @param isIpV4     Indicate whether query IpV4 address
	 * 
	 * @return return List<InetAddress>. if failed, the List is empty
	 */
	public List<InetAddress> getAddressList(String domainName, boolean isIpV4) {
		List<InetAddress> result = new ArrayList<InetAddress>();

		// make a query
		DnsPacket msg = query(isIpV4 ? DnsQueryType.A.getValue(): DnsQueryType.AAAA.getValue(), domainName);

		// if has response
		if (msg != null) {
			if (msg.addresses.size() > 0) {
				for (InetAddress a : msg.addresses)
					result.add(a);

			} else if (msg.cnames.size() > 0) { // if the domain has cname
				// query for the address of the cname
				for (String cname : msg.cnames) {
					List<InetAddress> cList = getAddressList(cname, isIpV4);
					for (InetAddress a : cList)
						result.add(a);
				}
			}
		}

		return result;
	}

	/**
	 * make a DNS query, get the first IpV4 address of specified domain name
	 * 
	 * 
	 * @param domainName The domain name, such as "www.some.com"
	 * 
	 * @return return InetAddress object. return null if failed.
	 */
	public InetAddress getIpV4Address(String domainName) {
		return getFirstAddress(domainName, true);
	}
	
	/**
	 * make a DNS query, get the first IpV6 address of specified domain name
	 * 
	 * 
	 * @param domainName The domain name, such as "www.some.com"
	 * 
	 * @return return InetAddress object. return null if failed.
	 */
	public InetAddress getIpV6Address(String domainName) {
		return getFirstAddress(domainName, false);
	}

	
	/**
	 * make a DNS query, get the first ip address of specified domain name
	 * 
	 * 
	 * @param domainName The domain name, such as "www.some.com"
	 * @param isIpV4     Indicate whether query IpV4 address
	 * 
	 * @return return InetAddress object. return null if failed.
	 */
	protected InetAddress getFirstAddress(String domainName, boolean isIpV4) {
		// query
		DnsPacket msg = query(isIpV4 ? DnsQueryType.A.getValue(): DnsQueryType.AAAA.getValue(), domainName);

		// if has response
		if (msg != null) {
			// if there is addresses
			if (msg.addresses.size() > 0)
				// return the first address
				return msg.addresses.get(0);

			else if (msg.cnames.size() > 0) { // if there is cname
				// query for the ip address of the first cname
				String cname = msg.cnames.get(0);
				return getFirstAddress(cname, isIpV4);
			}
		}

		return null;
	}

	/**
	 * Send UDP request to the DNS server, and return response bytes.
	 * 
	 * @param nameServer nameServer domain name or IP address
	 * @param request    request bytes
	 * @param length     length of bytes
	 * @param timeout    timeout in milliseconds
	 * @param retry      retry times
	 * @return return response bytes if success. return null if failed.
	 */
	public static byte[] udpQuery(String nameServer, byte[] request, int length, int timeout, int retry) {
		try {
			InetAddress host = InetAddress.getByName(nameServer);
			
			do {
				retry--; // decrease 1 after each try
				try {
					byte[] response = UdpClient.request(host, DNS.PORT, request, timeout);
					// if the response has same id as the request, it is correct response
					if (response != null && response[0] == request[0] && response[1] == request[1]) {
						return response;
					}
				} catch (Exception e) {
				}
	
			} while (retry > 0);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return null;
		
		
//		DatagramSocket udpSocket = null;
//
//		try {
//			InetAddress serverAddress = InetAddress.getByName(nameServer);
//			DatagramPacket sendPkt = new DatagramPacket(request, length, serverAddress, 53);
//			DatagramPacket receivePkt = new DatagramPacket(new byte[512], 512);
//
//			do {
//				retry -= 1; // decrease 1 after each try
//				try {
//					// compose UDP socket
//					udpSocket = new DatagramSocket();
//					udpSocket.connect(serverAddress, 53);
//					// send
//					udpSocket.send(sendPkt);
//					// set timeout
//					udpSocket.setSoTimeout(timeout);
//					// wait for response
//					udpSocket.receive(receivePkt);
//
//					DnsPacket response = new DnsPacket(receivePkt.getData());
//					// if the response has same id as the request, it is correct response
//					if (response.getByte(0) == request[0] && response.getByte(1) == request[1]) {
//						return response.getBytes();
//					}
//
//				} catch (Exception e) {
////					System.out.println(e);
//				} finally {
//					udpSocket.disconnect();
//					udpSocket.close();
//				}
//
//			} while (retry > 0);
//
//		} catch (Exception e) {
//
//		}
//
//		return null;

	}

	/** print debug information */
	protected void debug(Object... args) {
		if (debugLevel <= 0)
			return;

		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];

			if (i > 0)
				System.out.print(" ");

			if (arg instanceof byte[]) {
				byte[] bs = (byte[]) arg;
				for (int k = 0; k < bs.length; k++) {
					System.out.printf("%02X ", bs[k]);
				}
			} else {
				System.out.print(arg == null ? "null" : arg.toString());
			}

			if (i == args.length - 1)
				System.out.println();
		}
	}

}
