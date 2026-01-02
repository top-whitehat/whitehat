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
package top.whitehat.net;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import top.whitehat.LAN;
import top.whitehat.NetCard;
import top.whitehat.NetCardAddress;
import top.whitehat.NetUtil;
import top.whitehat.packet.MacAddress;
import top.whitehat.util.CommandLine;
import top.whitehat.util.Text;


/**
 * Read network information in Linux, such as gateway IP and MAC address.
 */
public class NetCardInfoLinux {
	
	/** Cache of result of uname -n */
	protected static String unameNodeCache = null;
	
	/** Cache of gateway Ip */
	protected static String gatewayIpCache = null;
		
	/** Check whether the OS is Kali Linux */
	protected static boolean isKali() {
		if (unameNodeCache == null) {
			unameNodeCache = CommandLine.run("uname", "-n").getOutput();
			unameNodeCache = unameNodeCache != null ? unameNodeCache.toLowerCase() : "";
		}
		return unameNodeCache.contains("kali");
	}

	/** Retrieve DNS servers */
	public static List<String> getDnsServers() {
		List<String> dnsServers = new ArrayList<>();

		try {
			// read information from the file: /etc/resolv.conf
			Text text = Text.fromFile("/etc/resolv.conf").grep("nameserver").split(" \t");
			for (int i = 0; i < text.rows(); i++) {
				String ip = text.cell(i, 1);
				if (NetUtil.isIpV4(ip) || NetUtil.isIpV6(ip))
					dnsServers.add(ip);
			}
		} catch (Exception e) {

		}
		return dnsServers;
	}

	/** Retrieve information of all network interface cards */
	public static List<NetCard> list() {
		List<NetCard> ret = new ArrayList<NetCard>();

		// get gateway ip and its mac
		String gatewayIP = getGatewayIP();
		String macAddress = getMACAddress(gatewayIP);

		InetAddress gatewayAddress;
		MacAddress gatewayMac = MacAddress.getByName(macAddress);
		try {
			gatewayAddress = InetAddress.getByName(gatewayIP);
			LAN.putMac(gatewayAddress, gatewayMac);
		} catch (Exception e) {
		}

		// get info from NetworkInterface
		try {
			Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
			while (nifs.hasMoreElements()) {
				NetworkInterface ni = nifs.nextElement();

				try {
					if (ni.isUp() && !ni.isLoopback()) {
						NetCardInfo info = new NetCardInfo();
						ret.add(info);
						info.name(ni.getName());
						info.displayName(ni.getDisplayName());
						info.mac(MacAddress.getByAddress(ni.getHardwareAddress()));
						// search ip address
						List<InterfaceAddress> addrs = ni.getInterfaceAddresses();
						for (InterfaceAddress addr : addrs) {
							NetCardAddress nAddr = new NetCardAddress(addr.getAddress(), addr.getNetworkPrefixLength());
							info.getNetcardAddresses().add(nAddr);
						}
						;

						try {
							InetAddress addr = InetAddress.getByName(gatewayIP);
							info.putGateWay(addr);
						} catch (Exception e) {
						}
					}
				} catch (SocketException e) {
					// "Error getting interface info: " + e.getMessage());
				}
			}

		} catch (SocketException e) {
			// "Error listing network interfaces: " + e.getMessage());
		}

		return ret;
	}
	
	
	
	/**
	 * Gets the default gateway IP address by reading /proc/net/route file
	 * 
	 * @return Gateway IP address as string, or null if not found
	 */
	public static String getGatewayIP() {
		if (gatewayIpCache != null) return gatewayIpCache;
		
		Text t;
		try {
			t = Text.fromFile("/proc/net/route");
		} catch (IOException e) {
			return null;
		}
		
		for(String line: t.rows) {
			String[] parts = line.trim().split("\\s+");
			if (parts.length >= 3) {
//				String iface = parts[0];
				String dest = parts[1];
				String gateway = parts[2];

				// Look for default route (destination 00000000) with non-zero gateway
				if (dest.equals("00000000") && !gateway.equals("00000000")) {
					// Convert hexadecimal IP to dotted decimal format
					gatewayIpCache = convertHexToIP(gateway);
					break;
				}
			}
		}
		
		if (gatewayIpCache == null) {
			gatewayIpCache = isKali() ?  getGatewayIPByCmdKali() : getGatewayIPByCommand();
		}
		return gatewayIpCache;
	}

	/**
	 * Converts hexadecimal IP representation to dotted decimal format
	 * 
	 * @param hexIP IP address in hexadecimal format (e.g., "0101A8C0" for
	 *              192.168.1.1)
	 * @return IP address in dotted decimal format
	 */
	private static String convertHexToIP(String hexIP) {
		// Handle endianness conversion (hex IP is in little-endian format)
		StringBuilder ip = new StringBuilder();
		try {
			// Process 2 hex characters at a time (1 byte) in reverse order
			for (int i = hexIP.length() - 2; i >= 0; i -= 2) {
				int octet = Integer.parseInt(hexIP.substring(i, i + 2), 16);
				ip.append(octet);
				if (i > 0)
					ip.append(".");
			}
		} catch (NumberFormatException e) {
			// "Invalid hex IP format: " + hexIP);
			return null;
		}
		return ip.toString();
	}
	
	/**
	 * Gets the MAC address for a given IP address by reading /proc/net/arp
	 * 
	 * @param ipAddress The IP address to find MAC for
	 * @return MAC address as string in XX:XX:XX:XX:XX:XX format, or null if not
	 *         found
	 */
	public static String getMACAddress(String ipAddress) {
		if (ipAddress == null)
			return null;

		Text t;
		try {
			t = Text.fromFile("/proc/net/arp");
		} catch (IOException e) {
			return null;
		}
		
		for(String line : t.rows) {
			String[] parts = line.trim().split("\\s+");
			if (parts.length >= 4) {
				String ip = parts[0];
				String mac = parts[3];

				// Check if IP matches and MAC is not empty
				if (ip.equals(ipAddress) && !mac.equals("00:00:00:00:00:00")) {
					return mac;
				}
			}
		}
		
		return getMACAddressByCommand(ipAddress);
	}
	
	public static String getMACAddressByCommand(String ipAddress) {
		String str = CommandLine.run("arp", "-n", ipAddress).getOutput();
		Text t = Text.of(str).delete("no entry").filter(ipAddress).split(" \t");
		System.out.println(t);
		String ret = t.rows() == 0 ? null : t.cell(0, 2);
		if(ret == null && "0.0.0.0".equals(ipAddress)) ret = MacAddress.BROADCAST.toString();
		return ret;
	}
	
	/**
	 * Alternative method to get gateway IP using system command execution
	 * 
	 * @return Gateway IP address from 'ip route' command
	 */
	private static String getGatewayIPByCommand() {
		Text t = CommandLine.run("ip route show default").text();
		
		for(String line : t.rows) {
			// Parse the default route line to extract gateway IP
			if (line.contains("default via")) {
				String[] parts = line.split("\\s+");
				for (int i = 0; i < parts.length - 1; i++) {
					if (parts[i].equals("via")) {
						return parts[i + 1];
					}
				}
			}
		}
		
		return getGatewayIPByCmdKali();
	}
	
	/** get gateway by command line: route -n */ 
	private static String getGatewayIPByCmdKali() {
		String str = CommandLine.run("route -n").getOutput();
		Text t = Text.of(str).delete("table").split(" \t");
		String gateway = t.cell(1, 1);
		
		// if gateway ip is a broadcast address
		if ("0.0.0.0".equals(gateway)) {
			// get ip and mask
			String ipStr = t.cell(1, 0).trim();
			String maskStr = t.cell(1, 2).trim();
			InetAddress ip = NetUtil.toInetAddress(ipStr);
			InetAddress mask = NetUtil.toInetAddress(maskStr);
			int prefixLength = mask == null ? 31 : NetUtil.calculatePrefixLength(mask);
			long maskLong = NetUtil.getIpV4Mask(prefixLength);	
			// try to find gateway ip in ARP table
			if (ip instanceof Inet4Address && prefixLength <= 32) {
				t = CommandLine.run("arp -n").text().delete("Flags").split(" \t");
				// for each item in ARP table
				for(int i=0; i<t.rows(); i++) {
					// get an ip string
					ipStr = t.cell(i, 0).trim();
					if (!NetUtil.isIpV4(ipStr)) continue;
					InetAddress ip1 = NetUtil.toInetAddress(ipStr);
					if(ip == null) continue; 
					
					// calculate its number (remove the mask)
					long ip1Long = NetUtil.ipToLong(ip1);
					long num = ip1Long & (~maskLong & 0xFFFFFFFF);
					if (num == 1 || num == 254) {
						return ipStr;
					}
				}
			}
		}
		
		return null;
	}
	
}
