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
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import top.whitehat.NetCard;
import top.whitehat.tools.ScanPort;
import top.whitehat.tools.ScanProgress;

public class Example12_ScanPort {

	public static void main(String[] args) throws UnknownHostException {
		// Open default network card 
		NetCard card = NetCard.inet();
		
		// Create ScanPort ScanIp instance
		ScanPort scanner = new ScanPort();
		
		// Subnet IP range of the LAN, which may looks like: "192.168.0.1/24"
		String ipRange = card.subnet();
		System.out.println("scan " + ipRange);
		
		// scan the LAN for port 80 or 443 
		scanner.scan(ipRange, "80,443");
		
		// Get the exists ip:port
		for(InetSocketAddress addr : scanner.getIpPorts()) {
			System.out.println(addr.getAddress().getHostAddress() + ":" + addr.getPort());
		}
	}

	/** get commonly used ports of one IP */
	static void scanCommonPorts() throws UnknownHostException {
		// Create a ScanPort instance on the default NetCard (that is connected to the
		// Internet)
		ScanPort scanner = new ScanPort();
		
		// the ip address to scan 
		InetAddress dst = InetAddress.getByName("192.168.100.100");
		
		// Scan common ports. This will not return until the scan task is finished.
		scanner.scan(dst, "common");

		// Get the result, and print it out
		for (int port : scanner.getExistPort(dst)) {
			System.out.println(port);
		}
	}
	
	
	/** scan multiple ports of one IP */
	static void scanPortsWithProgress() throws UnknownHostException {
		// Create a ScanPort instance on the default NetCard (that is connected to the
		// Internet)
		ScanPort scanner = new ScanPort();
		
		// (Optional) set speed: how many ports can be processed per second
		scanner.speed(5000);
		
		// (Optional) define a listener to handle progress updates
		scanner.onProgress(e->{
			ScanProgress p = (ScanProgress)e.getValue();
			System.out.print("\rscan " + p.count + "/" + p.total);
			if (p.count == p.total) {
				System.out.println("\r                        ");
			}
		});

		// the ip address to scan 
		InetAddress dst = InetAddress.getByName("192.168.100.100");

		// Scan specified ports. This will not return until the scan task is finished. 
		scanner.scan(dst, "20-100, 80, fast");

		// Get the result, and print it out
		for (int port : scanner.getExistPort(dst)) {
			System.out.println(port);
		}
	}
	
	

	
}
