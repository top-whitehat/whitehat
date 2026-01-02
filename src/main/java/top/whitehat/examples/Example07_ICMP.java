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
import top.whitehat.packet.IcmpPacket;

/** Example of ICMP protocol 
 *  <br><br>
 *  This routine demonstrates the ICMP protocol. It sends a ICMP request to 
 *  specified address and then receives a ICMP response from the address.
 */
public class Example07_ICMP {

	// A packet counter
	static int counter = 0;

	public static void main(String[] args) throws UnknownHostException {
		String targetIp = "8.8.8.8"; // target ip (please change to yours)

		// Open the network card that is connected to Internet
		NetCard card = NetCard.inet(); 
		card.filter("icmp"); // filter ICMP packets
		
		InetAddress srcAddr  = card.ip();
		InetAddress dstAddr = InetAddress.getByName(targetIp);

		// Define an event listener for network card startup.
		card.onStart(e -> {
			// Create an ICMP request
			IcmpPacket icmp = IcmpPacket.request(dstAddr, srcAddr, 1, 123, 32, 80);
			
			// Send the ICMP request to target ip
			card.sendPacket(icmp);
			System.out.println("Send ICMP request to " + targetIp);
		});
		
		// Define a listener to handle incoming packets
		card.onPacket(packet -> {
			// if it is an ICMP packet
			if (packet instanceof IcmpPacket) {
				IcmpPacket icmp = (IcmpPacket) packet;
				System.out.println(++counter + " " + icmp);
				
				// if the packet is an ICMP reply from target address
				if (icmp.isReply())  { // && dstAddr.equals(icmp.srcIp())) {
					// print and stop capture
					System.out.println("We got reply from " + targetIp + "!");
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
