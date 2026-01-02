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

import top.whitehat.Dump;
import top.whitehat.NetCard;


/** Dump means save the packets into a file (the file extension is ".pcap")
 *  <br><br>
 *  This example show how to capture packets and save them to the file.
 */
public class Example04_Dump {

	static int counter = 0;
	
	public static void main(String[] args) {
		// Find the network interface card that is connected to Internet
		NetCard card = NetCard.inet();
	
		// Open a dump file
		Dump dump = card.openDump("dump_records.pcap");
		System.out.println("open dump file: dump_records.pcap");
		
		// Define an event listener to handle incoming packets
		card.onPacket(packet -> {			
			// Write the packet into dump file
			dump.write(packet);
			
			System.out.println(++counter + " " + packet);
			if (counter >= 5) {
				dump.close();  // Close dump file
				card.stop();
			}
		});
		
		
		// Start packet capture. this will not return until stop() is called.
		card.start();
		
		// This will run after stop() is called.
		System.out.println("dumped count : " + counter);
		System.out.println("program ends.");
	}

}
