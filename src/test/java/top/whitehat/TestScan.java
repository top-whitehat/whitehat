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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import top.whitehat.tools.ScanIp;




public class TestScan {

	static int counter = 0;

	
	public static void main(String[] args) throws UnknownHostException {
		ScanIp scanIp = new ScanIp();

		scanIp.retry(1).onFind(e-> {
			System.out.println(e.getAddress());
		});
		
		scanIp.scan("192.168.100.1/24");
		
		System.out.println("---------");
		List<InetAddress> addresses = scanIp.exists();
		for(InetAddress addr : addresses) {
			System.out.println(addr.getHostAddress());
		}
		
		System.out.println("program end.");
	}

}
