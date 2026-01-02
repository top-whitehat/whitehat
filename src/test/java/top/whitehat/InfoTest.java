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
package top.whitehat;


import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;



public class InfoTest extends TestCase {
	
	public void testDomainInfo() {				
//		System.out.println(Info.getDomainInfo("csdn.net"));
		assertTrue(Info.getDomainInfo("csdn.net").containsKey("Domain Name"));
		assertTrue(Info.getDomainInfo("baidu.com").containsKey("Domain Name"));
		assertTrue(Info.getDomainInfo("189.cn").containsKey("Domain Name"));//		
	}
	
	public void testIpInfo() {		
		assertEquals("GD", Info.getIpInfo("14.145.9.93").get("region"));  // GD telecom
		assertEquals("GD", Info.getIpInfo("112.97.174.115").get("region")); // GD unicom
		assertEquals("GD", Info.getIpInfo("120.229.23.12").get("region")); // GD mobile 
	}
	
	public void testMacInfo() {	
		assertTrue(Info.getMacVendor("28:6F:B9:14:D2:7E").contains("Nokia"));
	}
	
	public void testMyIp() throws UnknownHostException {
		String myIpString = Info.myIp();
		InetAddress address = InetAddress.getByName(myIpString);
		assertTrue(NetUtil.isPublicIPAddress(address));		
	}
	
}
