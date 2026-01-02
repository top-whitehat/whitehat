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

import top.whitehat.NetCard;
import top.whitehat.dns.DNS;
import top.whitehat.dns.DnsPacket;
import top.whitehat.packet.UdpPacket;

/** Example of DNS protocol 
 *  <br><br>
 *  This routine demonstrates the DNS protocol. It sends a DNS request to 
 *  a DNS server and then receives a DNS response. 
 */
public class Example08_DNS {

	// A packet counter
	static int counter = 0;

	public static void main(String[] args) throws UnknownHostException {
		String targetIp = "8.8.8.8"; // DNS server ip (please change to yours)
		String domainName = "www.bing.com";  // the domain name to query DNS 
		
		// Open the network card that is connected to Internet
		NetCard card = NetCard.inet(); 
		card.filter("udp"); // filter UDP packets

		InetAddress srcAddr  = card.ip();
		InetAddress dstAddr = InetAddress.getByName(targetIp);
		
		// Define an event listener for network card startup.
		card.onStart(e -> {
			// create an DNS A query						
			int srcPort = 15333;    // random number (0-65535)			
			DnsPacket dns = DnsPacket.requestA(srcAddr, srcPort, dstAddr, 1, domainName);
			
			// send the DNS request
			card.sendPacket(dns);
			System.out.println("DNS request is sent to " + targetIp);
		}); 

		// Define a listener to handle incoming packets
		card.onPacket(packet -> {
			// If the packet is an UDP packet
			if (packet instanceof UdpPacket) {
				UdpPacket udp = (UdpPacket) packet;		
				
				// If the port is DNS port, then it should be a DNS packet
				if (udp.srcPort() == DNS.PORT  || udp.dstPort() == DNS.PORT) {
					// Create DnsPacket from the payload of the UdpPacket
					DnsPacket dns = udp.child(DnsPacket.class);
					System.out.println((++counter) + ":  " + dns);
					// If the packet is an DNS reply from DNS server
					if (dns.isReply() && dstAddr.equals(dns.srcIp())) {
						// print and stop capture
						System.out.println("We got reply from " + targetIp + "!");
						card.stop();
					}					
				}
			}
		});

		// Start packet capture. this will not return until stop() is called.
		card.start();

		// This will run after stop() is called.
		System.out.println("program ends.");
	}

}
