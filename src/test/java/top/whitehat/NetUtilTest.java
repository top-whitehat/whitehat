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

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;



public class NetUtilTest {

	@Test
	public void testIsLocalNetworkAddress() throws UnknownHostException {
		Map<String, Boolean> testIPs = new HashMap<String, Boolean>() {
			private static final long serialVersionUID = 1L;
			{
				put("127.0.0.1", true); // loopback -> true
				put("10.0.0.1", true); // private -> true
				put("172.16.0.1", true); // private -> true
				put("172.31.255.255", true); // private -> true
				put("192.168.1.100", true); // private -> true
				put("192.168.0.1", true); // private -> true
				put("8.8.8.8", true); // public -> false
				put("169.254.1.1", true); // link-local -> true
				put("203.0.113.5", true); // public test IP -> false
			}
		};

		for (String ip : testIPs.keySet()) {
			InetAddress inetAddress = InetAddress.getByName(ip);
			boolean isLocal = NetUtil.isLocalNetworkAddress(inetAddress);
			assertEquals(isLocal, testIPs.get(ip));
		}
	}
}
