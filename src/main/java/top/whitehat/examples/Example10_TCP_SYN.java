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
import top.whitehat.packet.TcpPacket;

/** Example of send TCP SYN packet and receive response.
 *  <br>
 *  <br>
 */
public class Example10_TCP_SYN {

	// A packet counter
	static int counter = 0;

	public static void main(String[] args) throws UnknownHostException {
		// Open the network card that is connected to Internet
		NetCard card = NetCard.inet(); 
		// filter tcp packets
		card.filter("tcp"); 
		
		String targetIp = InetAddress.getByName("www.baidu.com").getHostAddress();
		InetAddress dstAddr = InetAddress.getByName(targetIp);
		InetAddress srcAddr = card.ip();
		
		// Define an event listener for network card startup
		card.onStart(e -> {
			// Create an TCP SYN packet which means start tcp connection				
			int srcPort = 2042;
			int dstPort = 80;
			TcpPacket tcp = TcpPacket.createSyn(srcAddr, srcPort, dstAddr, dstPort, 1);
			// Send the TCP SYN packet
			card.sendPacket(tcp);
			System.out.println("TCP syn packet is sent to " + targetIp);
			System.out.println(++counter + " " + tcp);
		}); 

		// Define an event listener to handle incoming packets
		card.onPacket(packet -> {
			// If the packet is an TCP
			if (packet instanceof TcpPacket) {
				TcpPacket tcp = (TcpPacket) packet;
				
				// If the packet is an TCP packet from target address
				if (tcp.syn() && tcp.ack() && Match.address(packet, dstAddr)) {
					// print
					System.out.println(++counter + " " + tcp);
					System.out.println("We got SYN ACK reply from " + dstAddr.getHostName() + "!");
					card.stop();
				}
			}
		});

		// Start packet capture. this will not return until stop() is called.
		card.start();

		// This will run after stop() is called.
		System.out.println("program ends.");
	}

}
