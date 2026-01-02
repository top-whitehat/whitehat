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
package top.whitehat.net;

import java.net.InetAddress;

import top.whitehat.NetCardAddress;
import top.whitehat.NetUtil;





public class NetworkBroadcastCalculator {

    // ========== Test / Main Method ==========
    public static void main(String[] args) throws Exception {
//    	testGetBroadcast();
    	testNetCardAddress();
    }
    
    public static void testNetCardAddress() throws Exception {
    	String[] testCases = {
                "192.168.1.0/24",    // IPv4, valid
                "2001:db8::/64",     // IPv6, valid
                "10.0.0.1/33",       // IPv4, invalid prefix
                "invalid.ip/24",     // Invalid IP
                "2001:db8::/129",    // IPv6, invalid prefix
                "not.an.ip/",        // Missing prefix
                "another.bad/input"  // Missing '/'
        };

        for (String testCase : testCases) {
            System.out.println("\nParsing: " + testCase);
            try {
            	NetCardAddress.getByName(testCase);
            } catch (Exception e) {
            	System.out.println("❌ Failed to parse: " + testCase + " " + e.getMessage());
			}
        }
    }
    
    public static void testGetBroadcast() throws Exception {
        // --- IPv4 Example ---
        // IP: 192.168.1.100, Prefix: 24 (=> Subnet mask: 255.255.255.0)
        // Expected Broadcast: 192.168.1.255
        InetAddress ipv4Addr = InetAddress.getByName("192.168.1.100");
        int ipv4Prefix = 24;
        InetAddress ipv4Broadcast = NetUtil.getBroadcast(ipv4Addr, ipv4Prefix);
        System.out.println("IPv4 [" + ipv4Addr.getHostAddress() + "/" + ipv4Prefix + "] => Broadcast-like Address: " +
                (ipv4Broadcast != null ? ipv4Broadcast.getHostAddress() : "Calculation failed"));

        // --- IPv6 Example ---
        // IP: 2001:db8::1, Prefix: 64
        // Expected: Host bits (last 64 bits) all set to 1
        InetAddress ipv6Addr = InetAddress.getByName("2001:db8::1");
        int ipv6Prefix = 64;
        InetAddress ipv6LikeBroadcast = NetUtil.getBroadcast(ipv6Addr, ipv6Prefix);
        System.out.println("IPv6 [" + ipv6Addr.getHostAddress() + "/" + ipv6Prefix + "] => Host-bits-all-ones Address: " +
                (ipv6LikeBroadcast != null ? ipv6LikeBroadcast.getHostAddress() : "Calculation failed"));
    }
}