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

import top.whitehat.LAN;

public class DnsClientTest {

	public static void main(String[] args) {
//		System.out.println(DnsClient.of("192.168.100.212").getIpV4Address("hello"));
		test1();
	}
	
	static void test1() {
		String systemDNS = LAN.getDnsServer();
		System.out.println("Find System DNS: " + systemDNS);
		System.out.println();
		
		String domainName = "www.jostudio.com.cn";
		System.out.println(domainName + " IpV4: " + DNS.getIpV4(domainName));
		System.out.println(domainName + " IpV4: " + DnsClient.of(systemDNS).getIpV4Address(domainName));
		System.out.println(domainName + " IpV6: " + DnsClient.of("202.96.128.86").getIpV6Address(domainName));
		System.out.println();
		
		domainName = "www.baidu.com";
		System.out.println(domainName + " IpV4: " + DNS.getIpV4(domainName));
		System.out.println(domainName + " IpV4: " + DnsClient.of(systemDNS).getIpV4Address(domainName));
		System.out.println(domainName + " IpV6: " + DnsClient.of("202.96.128.86").getIpV6Address(domainName));
		
	}
}
