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
import top.whitehat.NetUtil;
import top.whitehat.packet.ArpPacket;

/** 
 * The Address Resolution Protocol (ARP) maps a host’s IP address to its MAC address, 
 * so the Ethernet frames can be correctly encapsulated and delivered on a LAN.<br>
 * <br>
 * A sender can sends an ARP Request for the destination IP→MAC mapping. <br>
 * All hosts on the same LAN will receive this ARP request; <br>
 * The host whose IP matches the request will sends back an ARP Reply with its MAC.
 */
public class Example06_ARP {

	static int counter = 0;

	public static void main(String[] args) throws UnknownHostException {
		String localIp = NetUtil.getInternetLocalAddress().getHostAddress(); // local ip of this machine (please change to yours)
		String targetIp = localIp.substring(0, localIp.lastIndexOf('.')) + ".1"; // target ip in the same LAN (please change to yours)

		InetAddress targetAddr = InetAddress.getByName(targetIp);

		// Open the network card of the local ip
		NetCard card = NetCard.of(localIp);
		card.filter("arp"); // filter ARP packets
		
		// Define an event listener for network card startup.
		card.onStart(e -> {
			// Compose an ARP request to the target address
			ArpPacket arp = ArpPacket.request(card.ip(), card.mac(), targetAddr);
			// Send the ARP request
			card.sendPacket(arp);
			System.out.println("Send ARP request to target ip : " + targetIp);
			System.out.println(++counter + " " + arp);
		});

		// Define a listener to handle incoming packets
		card.onPacket(packet -> {
			// If the packet is an ARP packet
			if (packet instanceof ArpPacket) {
				ArpPacket arp = (ArpPacket) packet;
				System.out.println(++counter + " " + arp);

				// If the packet is an ARP reply from target address
				if (arp.isReply() && targetAddr.equals(arp.senderIp())) {
					// print the answer
					System.out.println("We got an ARP reply from IP: " + targetIp + ", MAC:" + arp.senderMac());
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
