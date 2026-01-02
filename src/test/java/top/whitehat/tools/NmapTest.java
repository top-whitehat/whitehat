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
package top.whitehat.tools;

import java.net.UnknownHostException;




public class NmapTest {
	public static void main(String[] args) throws UnknownHostException {
//		String host = Config.remote.host;
//		InetAddress address = InetAddress.getByName(host); 
//		System.out.println(Nmap.scanPort("www.baidu.com", "80,443"));
//		System.out.println(Nmap.scanOS("www.jostudio.com.cn"));
//		System.out.println(Nmap.scanService("www.jostudio.com.cn", "80,443,8080"));
		
//		JSON ret = Nmap.scanIp("100.127.209.189/32", Nmap.SCAN_ARP).json();
		
//		JSON ret = Nmap.scanPort("www.jostudio.com.cn", "80,443").json();
		
//		JSON ret = Nmap.scanService("www.jostudio.com.cn", "80,443").json();
		
//		JSON ret = Nmap.scanOs("www.jostudio.com.cn").json();
		
////		System.out.println(ret);
//		for(String script : Nmap.scripts()) {
//			System.out.println(script);
//		}
		
//		System.out.println(Nmap.runScript("http-title", "www.bing.com -p 80").exec() ); //
//				.text("http-title").extractWord(": "));
		
		
		System.out.println(Nmap.script("vuln", "100.127.209.122").exec() ); //
	}
}
