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
import java.util.List;

import top.whitehat.NetCard;
import top.whitehat.tools.ScanIp;
import top.whitehat.tools.ScanProgress;

public class Example11_ScanIp {

	public static void main(String[] args) throws UnknownHostException {
		// Open default network card 
		NetCard card = NetCard.inet();
		
		// Create a ScanIp instance
		ScanIp scanner = new ScanIp(card);
		
		// Subnet IP range of the LAN, which may looks like: "192.168.0.1/24"
		String ipRange = card.ip().getHostAddress() + "/" + card.getNetworkPrefixLength(); 
		System.out.println("scan " + ipRange);
		
		// Scan an ip range. This will not return until the scan task is finished.
		scanner.scan(ipRange);

		// Get the exists IP, and print it out
		for (InetAddress addr : scanner.exists()) {
			System.out.println(addr.getHostAddress());
		}
		
		// Get the not exists IP, and print it out
//		for (InetAddress addr : scanner.notExists()) {
//			System.out.println(addr.getHostAddress());
//		}

	}

	/** Scan an InetAddress array */
	static void example2() throws UnknownHostException {
		// Create a ScanIp object on the default NetCard
		ScanIp scanIp = new ScanIp();

		// An InetAddress array to be scanned
		InetAddress[] addresses = new InetAddress[4];
		addresses[0] = InetAddress.getByName("192.168.100.185");
		addresses[1] = InetAddress.getByName("192.168.100.186");
		addresses[2] = InetAddress.getByName("192.168.100.204");
		addresses[3] = InetAddress.getByName("192.168.100.211");

		// Start scan, this will not return until the scan task is finished.
		scanIp.scan(addresses);

		// Print the addresses
		for (InetAddress addr : addresses) {
			System.out.println(addr.getHostAddress());
		}
	}

	/** Scan an Ip range, handle events */
	static void example3() {
		// Find network card of local ip on this computer
		NetCard netCard = NetCard.of("192.168.100.211"); // please change to your local ip

		// Create a ScanIp object on the NetCard
		ScanIp scanIp = new ScanIp(netCard);

		// (Optional) set speed: how many IP addresses can be processed per second
		scanIp.speed(500);

		// (Optional) define a listener to handle progress updates
		scanIp.onProgress(e -> {
			// get progress from e.value, print it
			ScanProgress p = (ScanProgress) e.getValue();
			System.out.print("\rScanning " + p.count + " of total " + p.total);

			// (optional) you can stop scan any time
			if (p.count > 100) {
				scanIp.stop();
				System.out.print("\nCanceled.\r\n");
			}

			// when total == count, it means all works is done
			if (p.total == p.count) {
				System.out.print("\rDone! scanned " + p.count + "\n");
			}
		});

		// (Optional) define a handler when an ip is found
		scanIp.onFind(e -> {
			// get ip address from e
			// InetAddress ip = e.getAddress();
			// TODO: process the ip
		});

		// Start scan, this will not return until the scan task is finished.
		scanIp.scan("192.168.100.1/24");

		// After scan, get the results
		List<InetAddress> addresses = scanIp.exists();

		// Print the addresses
		for (InetAddress addr : addresses) {
			System.out.println(addr.getHostAddress());
		}
	}

}
