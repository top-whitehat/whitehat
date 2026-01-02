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
package top.whitehat;

import top.whitehat.packet.TcpPacket;
import top.whitehat.packet.UdpPacket;

public class TestCapture {

	public static String localIp = "";

	public static int count = 0;

	public static void main(String[] args) {
		// find local ip which connects to Internet
		localIp = NetUtil.getInternetLocalAddress().getHostAddress();

		simpleCapture();
//		tcpCapture();
//		udpCapture();
	}
	
	public static void simpleCapture() {
		NetCard.capture(packet -> {
			System.out.println(++count + " " + packet);
		});
	}

	public static void standardCapture() {
		// find a network interface card
		NetCard card = NetCard.of(localIp);
		
		// define a handler when packet received
		card.onPacket(packet -> {
			System.out.println(++count + " " + packet);
		});
		
		// start packet capture
		card.start();
	}

	public static void tcpCapture() {
		// find a network interface card
		NetCard card = NetCard.of(localIp);
		
		// define packet filter
		String filterStr = "tcp and (dst host " + localIp + " or src host " + localIp + ")";
		card.filter(filterStr);
		
		// define a handler when packet received
		card.onPacket(packet -> {
			if (packet instanceof TcpPacket)
				System.out.println(++count + " " + packet);
		});
		
		// start packet capture
		card.start();
	}

	public static void udpCapture() {
		// find a network interface card
		NetCard card = NetCard.of(localIp);
		
		// define packet filter
		String filterStr = "udp and (dst host " + localIp + " or src host " + localIp + ")";
		card.filter(filterStr);
		
		// define a handler when packet received
		card.onPacket(packet -> {
			if (packet instanceof UdpPacket)
				System.out.println(++count + " " + packet);
		});
		
		// start packet capture
		card.start();

	}

}
