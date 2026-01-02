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

import java.net.UnknownHostException;

import top.whitehat.Match;
import top.whitehat.NetCard;
import top.whitehat.NetUtil;
import top.whitehat.packet.DhcpPacket;
import top.whitehat.packet.UdpPacket;

/** Example of DHCP protocol 
 *  <br>
 *  <br>
 *  This routine demonstrates the DHCP protocol. It sends a DHCP request via broadcast 
 *  and then receives a DHCP response.
 */
public class Example09_DHCP {

	// A packet counter
	static int counter = 0;
	
	static int taskId = 0; 

	public static void main(String[] args) throws UnknownHostException {
		// Open the network card that is connected to Internet
		NetCard card = NetCard.inet(); 
		
		// Set filter DHCP use port 67 or 68
		card.filter("udp port 67 or udp port 68"); 
		
		// Define an event listener for network card startup
		card.onStart(e -> {
			// Create an DHCP request						
			DhcpPacket dhcp = DhcpPacket.request(card.mac());			
			// Send the DHCP request
			card.sendPacket(dhcp);
			System.out.println("DHCP request is sent : " + dhcp);
			
			// Wait a while, if there is still no reply, then stop packet capture
			taskId = NetUtil.setTimeout(()->{
				System.out.println("no reply, timeout");
				card.stop(); // stop packet capture
			}, 3000);  // the timeout is 3000 milliseconds
		}); 

		// Define a listener to handle incoming packets
		card.onPacket(packet -> {
			// If the packet is an ICMP
			if (packet instanceof UdpPacket) {
				UdpPacket udp = (UdpPacket) packet;				
				// If match DHCP server port or client port, then it should be a DHCP
				if (Match.port(udp, DhcpPacket.SERVER_PORT, DhcpPacket.CLIENT_PORT)) {
					
					// Create DhcpPacket from the payload of the UdpPacket
					DhcpPacket dhcp = udp.child(DhcpPacket.class);
					System.out.println((counter++) + ":  " + dhcp);
					
					// If this DHCP packet is a reply to current network card
					if (dhcp.isReply() && Match.mac(dhcp.clientMac(), card.mac())) {					
						card.stop(); // stop packet capture
						NetUtil.clearTimeout(taskId); // clear task of setTimeout()
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
