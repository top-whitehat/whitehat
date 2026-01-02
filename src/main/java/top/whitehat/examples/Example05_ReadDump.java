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

/**
 * This example show how to read packets from the dump file.
 */
public class Example05_ReadDump {

	static int counter = 0;

	public static void main(String[] args) {
		// Find the network interface card that is connected to Internet
		NetCard card = NetCard.inet();

		// Define an event listener for incoming packets (from the dump file)
		card.onPacket(packet -> {
			counter++;
			System.out.println("dump " + counter + " " + packet);

			if (counter >= 8)
				card.stop(); // stop() will stop reading the file
		});

		System.out.println("Read dump file dump_records.pcap ");
		
		// Read dump file, then for each packet in the file, card.onPacket() is called
		card.readDump("dump_records.pcap");

		// This will run after finish reading the file or card.stop() is called.
		System.out.println("program ends.");
	}

}
