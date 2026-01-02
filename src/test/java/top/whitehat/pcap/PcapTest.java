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
package top.whitehat.pcap;

import java.net.InetAddress;

import top.whitehat.NetUtil;




public class PcapTest {

	static int counter = 1;


	public static void main(String[] args) {
		// check whether libpcap is installed.
		if (!Pcap.isExists()) {
			System.out.println("libpcap not exists, please install first");
			return;
		}

		// list all PcapNetworkInterface
//		List<PcapNetworkInterface> nifs;
//		nifs = Pcap.findAllDevs();
//		for(PcapNetworkInterface nif : nifs) {
//			System.out.println(nif);
//		}		
//		System.out.println("-----------------");

		// find local ip that is connected to Internet
		InetAddress localIp = NetUtil.getInternetLocalAddress();
		if (localIp == null) 
			throw new RuntimeException("cannot find network card that is connected to Internet");
		
		// open PcapNetworkInterface
		Pcap pcap = Pcap.of(localIp.getHostAddress());
		System.out.println("find " + pcap);

		// open line
		pcap.openLive(10);
//		pcap.filter("tcp and dst port 80");
		pcap.filter("ip");
		
//		PcapDumper dumper = pcap.openDump("dump1.pcap");
//
//		// start capture
//		try {
//			pcap.loop(-1, pkt -> {
//				dumper.dump(pkt);
//				printBytes(pkt.getRawData());
//				if (++counter > 3)
//					pcap.breakLoop();
//			});
//		} catch (InterruptedException e) {
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		dumper.close();
		
		// close pcap
		pcap.close();
		System.out.println("program end");
	}

	static void printBytes(byte[] bs) {
		if (bs == null) {
			System.out.println("null");
			return;
		}

		for (int i = 0; i < bs.length; i++) {
			System.out.print(String.format("%02x ", bs[i]));
		}

		System.out.println();
	}

}
