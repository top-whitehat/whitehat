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

/**
 * NetBIOS send name service query and got reply
 */
public class Example14_NetBIOS {

	static int counter = 0;
	
	public static void main(String[] args) throws UnknownHostException {
		String localIp = NetUtil.getInternetLocalAddress().getHostAddress(); // local ip of this machine (please change to yours)
		String targetIp = localIp.substring(0, localIp.lastIndexOf('.')) + ".220"; // target ip in the same LAN (please change to yours)		
		
		
		// Open the network card that is connected to Internet
		NetCard card = NetCard.inet(); 
		card.filter("udp"); // filter UDP packets

		InetAddress srcAddr  = card.ip();
		InetAddress dstAddr = InetAddress.getByName(targetIp);
		
		// Define an event listener for network card startup.
		card.onStart(e -> {
			System.out.println("start " + localIp);
			int srcPort = 15333;    // random number (0-65535)		
			// create an NetBIOS name service query packet		
			NetBIOS req = NetBIOS.query(srcAddr, srcPort, dstAddr, 1, "*");
			
			// send the NetBIOS request
			card.sendPacket(req);
			System.out.println("NetBIOS request is sent to " + targetIp);
		}); 

		// Define a listener to handle incoming packets
		card.onPacket(packet -> {
			// if the packet match NetBIOS PORT
			if (Match.addressAndPort(packet, srcAddr, NetBIOS.PORT)) {
				// get NetBIOS packet from the payload of the UdpPacket
				NetBIOS nb = packet.child(NetBIOS.class);
				
				// get NetBIOS name!!! 
				String nbName = nb.getName();
				
				System.out.println((++counter) + ":  " + nb);
				
				// if the packet is a NetBIOS reply from dst address
				if (nb.isReply() && dstAddr.equals(nb.srcIp())) {
					System.out.println("We got reply from " + targetIp + " : " + nbName);
					card.stop(); //stop capture
				}
			}

		});

		// Start packet capture. this will not return until stop() is called.
		card.start();

		// this will run after stop() is called.
		System.out.println("program ends.");	

	}

}
