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
import top.whitehat.packet.TcpPacket;

/**
 * This example introduces filter.<br>
 * 
 * A filter is a string to select packets <br>
 * 
 * <h3>Examples:</h3> <br>
 * <b>"udp port 53"</b> means select UDP packet and port is 43 <br>
 * <br>
 * <b>"tcp (port 80 or port 433)"</b> means select TCP packet and port is 80 or
 * 443 <br>
 * <br>
 * 
 * 
 * The filter string is written in a BPF(Berkeley Packet Filter) format, which
 * is widely used in Linux OS, in libpcap library(or winpcap library in Windows)
 * 
 * <h3>Documentation</h3> SEE:
 * <a href="https://www.tcpdump.org/manpages/pcap-filter.7.html">packet filter
 * syntax</a>
 * 
 * 
 */
public class Example03_Filter {

	static int counter = 0;

	public static void main(String[] args) {
		// Find local ip that is connected to Internet
		String localIp = NetUtil.getInternetLocalAddress().getHostAddress();

		// Open the network interface card of the local ip
		NetCard card = NetCard.of(localIp);

		// Define filter: only tcp and its address is local ip
		String filterStr = "tcp and host " + localIp;
		card.filter(filterStr);

		// Print explanation
		System.out.println("This example uses the filter: \"" + filterStr + "\"");
		String msg = "select TCP packet and its source or destionaion address is ";
		System.out.println(msg + localIp);
		System.out.println();

		// Define an event listener to handle incoming packets
		card.onPacket(packet -> {

			// check the packet type
			if (packet instanceof TcpPacket) {

				// print packet
				System.out.println(++counter + " " + packet);

				// stop capture after 5 packets is received
				if (counter >= 5)
					card.stop();
			}

		});

		// Start packet capture. this will not return until stop() is called.
		card.start();

		// This will run after stop() is called.
		System.out.println("\nprogram ends.\n");

		System.out.println("BPF(Berkeley Packet Filter) is a language for filtering packets.");
		System.out.println("SEE: https://www.tcpdump.org/manpages/pcap-filter.7.html");
		System.out.println();
	}

}
