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
import top.whitehat.dns.NetBIOS;
import top.whitehat.tools.ScanIp;

/**
 * Scan IPs of local network, and find NetBIOS names of exists ip.
 */
public class Example15_NetBIOS_Scan {

	static int counter = 0;

	public static void main(String[] args) throws UnknownHostException {
		String localIp = NetUtil.getInternetLocalAddress().getHostAddress(); // local ip of this machine (please change
		String ipRange = localIp + "/24";																	// to yours)

		// Create a ScanIp instance
		ScanIp scanner = new ScanIp(NetCard.of(localIp));
		// Scan an ip range.
		scanner.scan(ipRange);

		// Open the network card
		NetCard card = NetCard.of(localIp);
		InetAddress srcAddr = card.ip();
		
		// Define an event listener for network card startup.
		card.onStart(e ->  {
			// Get the exists IP, and print it out
			for (InetAddress addr : scanner.exists()) {
				NetBIOS req = NetBIOS.query(srcAddr, 15333, addr, 1, "*");
				card.sendPacket(req);
			}
		});
		
		// Define a listener to handle incoming packets
		card.onPacket(packet -> {
			// if the packet smatch NetBIOS PORT
			if (Match.port(packet, NetBIOS.PORT)) {
				// get NetBIOS packet from the payload of the UdpPacket
				NetBIOS nb = packet.child(NetBIOS.class);

				// get NetBIOS name!!! 
				String nbName = nb.getName();
				
				// if the packet is a NetBIOS reply
				if (nb.isReply()) {
					System.out.println(nb.srcIp().getHostAddress() + " :  " + nbName);
				}
			}

		});

		// Start packet capture. this will not return until stop() is called.
		card.start();

		// this will run after stop() is called.
		System.out.println("program ends.");

	}

}
