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

import com.sun.jna.Platform;

import top.whitehat.net.NetCardInfo;
import top.whitehat.packet.MacAddress;
import top.whitehat.util.CommandLine;
import top.whitehat.util.Text;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import  java.net.Inet4Address;
import  java.net.Inet6Address;
import java.util.HashMap;
import java.util.List;

/** Utilities for LAN(Local Area Network) */
public class LAN {
	
	/** Retrieve DNS server */
	public static String getDnsServer() {
		return NetCardInfo.getDnsServer();
	}

	/** A map stores InetAddress->MAC address pairs */
	protected static HashMap<InetAddress, MacAddress> ipMacMap = new HashMap<InetAddress, MacAddress>();
		
	/** Set InetAddress->MAC address pair */
	public static void putMac(InetAddress addr, MacAddress mac) {
		if (addr == null)
			return;
		
		if (mac == null)
			ipMacMap.remove(addr);
		else
			ipMacMap.put(addr, mac);
	}

	/** Get MAC address by specified InetAddress, return null if not found */
	public static MacAddress getMac(InetAddress addr) {
		return getMacOrDefault(addr, null);
	}

	/** Get MAC address by specified InetAddress, return defaultMac if not found */
	public static MacAddress getMacOrDefault(InetAddress addr, MacAddress defaultMac) {
		return ipMacMap.getOrDefault(addr, defaultMac);
	}
	
	/** Find gateway InetAddress */
	public static InetAddress findGateway(InetAddress addr) {
		List<NetCard> infos = NetCard.list();
		if (infos != null) {
			for(NetCard info: infos) {
				if (info.isInSameSubnet(addr)) {
					for(NetCardAddress nAddr : info.getNetCardAddresses()) {
						if (nAddr.getGateway() != null)
							return nAddr.getGateway();
					}
				}
			}
		}
		return null;
	}
	
	/** initialize LAN */
	private static void initMAC() {
		if (Platform.isWindows()) {
			// get MAC addresses from command line: arp -a
			Text t = CommandLine.run("arp -a").text("-").delete("--").split(" \t");
			for(int r = 0; r < t.rows(); r++) {
				try {
					String sIp = t.cell(r,  0);
					String sMac = t.cell(r,  1);
					putMac(InetAddress.getByName(sIp), MacAddress.getByName(sMac));
				} catch (UnknownHostException e) {
				}
			}
//			TextTable table = CommandLine.run("arp -a").rows("-").delete("--").split(" \t");
//			for(TextRow row : table) {
//				try {
//					String sIp = row.get(0);
//					String sMac = row.get(1);
//					putMac(InetAddress.getByName(sIp), MacAddress.getByName(sMac));
//				} catch (UnknownHostException e) {
//				}
//			}
		} else if (Platform.isLinux()) {
			//get MAC addresses from file /proc/net/arp
			String filePath = "/proc/net/arp";
			Text t;
			try {
				t = Text.readFile(filePath).deleteEmpty().split(" \t").setFieldNames(0);
				for(int row=0; row < t.rows(); row++) {
					try {
						String sIp = t.cell(row, 0); // row.get(0); // ip address
						String sMac = t.cell(row, 3); //row.get(3); // MAC address
						putMac(InetAddress.getByName(sIp), MacAddress.getByName(sMac));
					} catch (UnknownHostException e) {
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}			
			
//			TextTable table = TextRow.fromFile(filePath).deleteEmpty().split(" \t").setFieldNames(0);			
//			for(TextRow row : table) {
//				try {
//					String sIp = row.get(0); // ip address
//					String sMac = row.get(3); // MAC address
//					putMac(InetAddress.getByName(sIp), MacAddress.getByName(sMac));
//				} catch (UnknownHostException e) {
//				}
//			}
		}
	}

	
	static {
		initMAC();
		NetCard.list();
	}

	


	/**
	 * Utility method to detect IP version of an InetAddress
	 */
	public static String getIPVersion(InetAddress ip) {
		if (ip instanceof Inet6Address) {
			return "IPv4";
		} else if (ip instanceof Inet4Address) {
			return "IPv6";
		} else {
			return "Unknown";
		}
	}

}
