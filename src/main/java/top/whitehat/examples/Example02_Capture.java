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

import top.whitehat.NetCard;
import top.whitehat.NetUtil;

/** This example shows how to capture packets. */
public class Example02_Capture {

	static int counter = 0;

	public static void main(String[] args) {
		// Find local IP that is connected to Internet
		String ipString = NetUtil.getInternetLocalAddress().getHostAddress();
		System.out.println("Network card: " + ipString);

		// Open the network interface card by the IP string
		NetCard card = NetCard.of(ipString);

		// Define an event listener to handle incoming packets
		card.onPacket(packet -> {
			// print the packet
			System.out.println(counter + " " + packet);

			// After 10 packets is received
			if (++counter >= 1000000)
				card.stop(); // stop capture
		});

		// Start packet capture. this will not return until card.stop() is called.
		card.start();

		// this will run after card.stop() is called.
		System.out.println("program ends.");
	}

}
