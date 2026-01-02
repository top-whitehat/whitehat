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

import top.whitehat.LAN;
import top.whitehat.packet.PacketUtil;

public class DNS {
	
	/** default DNS port */
	public final static int PORT = 53;

	// DNS Servers
	public static InetAddress CLOUNDFLARE = PacketUtil.toInetAddress(1, 1, 1, 1); // 1.1.1.1
	public static InetAddress GOOGLE = PacketUtil.toInetAddress(8, 8, 8, 8); // "8.8.8.8";
	public static InetAddress QUAD9 = PacketUtil.toInetAddress(9, 9, 9, 9); // "9.9.9.9";
	public static InetAddress OPEN_DNS = PacketUtil.toInetAddress(208, 67, 222, 222); // "208.67.222.222";

	public static InetAddress ALIYUN = PacketUtil.toInetAddress(223, 5, 5, 5); // "223.5.5.5";
	public static InetAddress TENCENT = PacketUtil.toInetAddress(119, 29, 29, 29); // "119.29.29.29";
	public static InetAddress CNNIC = PacketUtil.toInetAddress(1, 2, 4, 8); // "1.2.4.8";
	public static InetAddress CHINA_TELECOM = PacketUtil.toInetAddress(202, 96, 128, 86); // "202.96.128.86";
	public static InetAddress CHINA_UNICOM = PacketUtil.toInetAddress(123, 123, 123, 123); // "123.123.123.123";
	public static InetAddress CHINA_MOBILE = PacketUtil.toInetAddress(221, 130, 33, 60); // "221.130.33.60";
	public static InetAddress DNS_114 = PacketUtil.toInetAddress(114, 114, 114, 114); // "114.114.114.114";

	
	/** Get default DNS server of this computer */
	public static String getDefaultDnsServer() {
		String server = LAN.getDnsServer();
		return server == null ? "8.8.8.8" : server;
	}

	/**
	 * Make a DNS query, get the first IpV4 address of specified domain name
	 * 
	 * 
	 * @param domainName The domain name, such as "www.some.com"
	 * 
	 * @return return IpV4 string. return null if failed.
	 */
	public static String getIpV4(String domainName) {
		DnsClient client = new DnsClient();
		InetAddress address = client.getFirstAddress(domainName, true);
		return address != null ? address.getHostAddress() : null;		
	}

	/**
	 * Make a DNS query, get the first IpV6 address of specified domain name
	 * 
	 * 
	 * @param domainName The domain name, such as "www.some.com"
	 * 
	 * @return return IpV6 string. return null if failed.
	 */
	public static String getIpV6(String domainName) {
		DnsClient client = new DnsClient();
		InetAddress address = client.getFirstAddress(domainName, false);
		return address != null ? address.getHostAddress() : null;
	}

}
