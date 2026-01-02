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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import top.whitehat.NetUtil;

import static org.junit.Assert.*;

import java.net.InetAddress;

/**
 * Test code for PcapHandle class
 */
public class PcapHandleTest {
	static PcapNetworkInterface nif;
	static Pcap pcap;
	
	// sleep a while
	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}
	
	@BeforeClass
	public static void setUpOnce() {
		// return the local ip that is connected to Internet 
		InetAddress localIp = NetUtil.getInternetLocalAddress();
		assertTrue(localIp != null);
		
		// find PcapNetworkInterface of the localIp
		pcap = Pcap.of(localIp.getHostAddress());
		assertTrue(nif != null);
		System.out.println(nif);
		
		// open live handle
		pcap.openLive(100);
		
        //set filter
		pcap.filter("ip");
        sleep(500);
    }
	
	@AfterClass
	public static void close() {
		if (pcap != null) 
			pcap.close();
	}

	@Test
    public void testGetSnapLen() {
        assertTrue(pcap.snaplen() > 0);
    }

	@Test
    public void testGetDataLinkType() {
        assertTrue(pcap.dataLinkType() == Pcap.DLT_ETHERNET);
    }

	@Test
    public void testSendPacket() {
    	byte[] packet = new byte[80];
    	pcap.sendPacket(packet);
        assertTrue(true);
    }

	@Test
    public void testGetStats() {
    	PcapStat stat = pcap.getStats();
    	System.out.println(stat);
        assertTrue(true);
    }
}