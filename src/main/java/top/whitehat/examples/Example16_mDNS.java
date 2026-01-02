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
package top.whitehat.examples;

import java.net.InetAddress;
import java.net.UnknownHostException;

import top.whitehat.Match;
import top.whitehat.NetCard;
import top.whitehat.NetUtil;
import top.whitehat.dns.mDNS;

/** Example of mDNS protocol 
 *  <br><br>
 *  This routine demonstrates the mDNS protocol. 
 *  It sends a mDNS query and then receives a mDNS response. 
 */
public class Example16_mDNS {

	// A packet counter
	static int counter = 0;

	public static void main(String[] args) throws UnknownHostException {
//		String domainName = "diskstation.local";  // the local domain name to query DNS 
		InetAddress dstAddress =  NetUtil.toInetAddress("192.168.100.209");
		
		// Open the network card
		NetCard card = NetCard.inet(); 
		card.filter("udp"); // filter UDP packets
		
		// Define an event listener for network card startup.
		card.onStart(e -> {
			int srcPort = 15333;    // random number (0-65535)	
			// create an mDNS A query
			mDNS dns;
			
			// https://cloud.tencent.com/developer/article/1890214
//			dns = mDNS.requestA(card.ip(), srcPort, domainName);
			
			String serviceName = null; // "_companion-link._tcp.local"; // "airplay._tcp.local";
			dns = mDNS.findService(card.ip(), srcPort, dstAddress, serviceName);
			
			// send the DNS request
			card.sendPacket(dns);
			System.out.println("mDNS request is sent to " + dstAddress);
		}); 

		// Define a listener to handle incoming packets
		card.onPacket(packet -> {
			// If the packet is an mDNS packet
			if (Match.port(packet, mDNS.PORT)) {
				// get DnsPacket from the payload of the UdpPacket
				mDNS dns = packet.child(mDNS.class);
				System.out.println((++counter) + ":  " + dns);

				// If the packet is an DNS reply from DNS server
				if (dns.isReply() && dns.dstIp().equals(card.ip())) {
					// print and stop capture
					System.out.println("We got reply from " + dns.srcIp() + "!");
//					card.stop();
				}					
			}
		});

		// Start packet capture. this will not return until stop() is called.
		card.start();

		// This will run after stop() is called.
		System.out.println("program ends.");
	}

}
