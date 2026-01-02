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
package top.whitehat.udp;

import java.util.HashMap;
import java.util.Map;

import top.whitehat.NetCard;
import top.whitehat.dns.DnsPacket;
import top.whitehat.packet.IHasIpPort;







public class UdpServerTest {
	
	static Map<String, String> answers = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("hello", "Hi, boys");
			put("who are you?", "I'm AI boy.");
		}
	};
	
	
	static String getAnswer(String question) {
		return answers.getOrDefault(question.toLowerCase(), "I dont konw");
	}

	public static void main(String[] args) {
		int port = 53;
		NetCard card;
//		card = new DatagramNetCard(port);
		card = NetCard.inet();
		card.timeout(10);
		
		UdpServer server = new UdpServer(card);
		
		server.onStart(e->{
			System.out.println("UDP server start at " 
					+ card.ip().getHostAddress() + ":"+ port);
		});
		
		server.onPacket(packet->{
			
			if (packet instanceof IHasIpPort) {
				if (((IHasIpPort)packet).hasPort(53)) {
					DnsPacket dns = packet.getPacket(DnsPacket.class, 53);
					System.out.println(dns);
				}
			}
			
//			String request  = new String(packet.payload());
//			System.out.println(packet.srcIpPortStr() + ": " + request);
//			String response = getAnswer(request);
//			System.out.println(packet.dstIpPortStr() + ": " + response);
//			server.reply(packet, response.getBytes());
		});
		
		server.start();
	}
}
