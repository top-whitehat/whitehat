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

import top.whitehat.packet.IHasIp;
import top.whitehat.packet.IHasIpPort;
import top.whitehat.packet.IHasMac;
import top.whitehat.packet.MacAddress;
import top.whitehat.packet.Packet;

/** Provides utility methods for matching. */
public class Match {

	/**
	 * Checks whether the packet's source or destination port matches one of the
	 * provided ports.
	 * 
	 * @param packet The packet to inspect
	 * @param ports  The ports to match against
	 * @return return true if source or destination port is in the set; false
	 *         otherwise
	 */
	public static boolean port(Packet packet, int... ports) {
		if (packet instanceof IHasIpPort) {
			IHasIpPort p = (IHasIpPort) packet;
			for (int port : ports) {
				if (p.srcPort() == port || p.dstPort() == port)
					return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the packet's source or destination address and port matches
	 * 
	 * @param packet The packet to inspect
	 * @param addr   The address to match against
	 * @param port   The port to match against
	 * 
	 * @return return true if match; false otherwise
	 */
	public static boolean addressAndPort(Packet packet, InetAddress addr, int port) {
		if (packet instanceof IHasIpPort) {
			IHasIpPort p = (IHasIpPort) packet;
			if (addr == null || addr.equals(p.srcIp()) || addr.equals(p.dstIp())) {
				if (p.srcPort() == port || p.dstPort() == port)
					return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the packet's source or destination address and port matches
	 * 
	 * @param packet The packet to inspect
	 * @param addr1  The first possible address to match against
	 * @param addr2  The first possible address to match against
	 * @param port1  The first possible port to match
	 * @param port2  The second possible port to match
	 * @return return true if match; false otherwise
	 */
	public static boolean addressAndPort(Packet packet, InetAddress addr1, InetAddress addr2, int port1, int port2) {
		if (packet instanceof IHasIpPort) {
			IHasIpPort p = (IHasIpPort) packet;
			// if one of the addr1, addr2 is match
			if ((addr1 == null || addr1.equals(p.srcIp()) || addr1.equals(p.dstIp())) //
					|| (addr2 == null || addr2.equals(p.srcIp()) || addr2.equals(p.dstIp()))) {
				// if one of the port1, port2 is match
				if ((p.srcPort() == port1 || p.dstPort() == port1) //
						|| (p.srcPort() == port2 || p.dstPort() == port2))
					return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the packet's source or destination IP matches one of the
	 * provided InetAddresses.
	 * 
	 * @param packet The packet to inspect
	 * @param addr   The set of InetAddress to match against
	 * @return return true if source or destination IP is in the set; false
	 *         otherwise
	 */
	public static boolean address(Packet packet, InetAddress... addresses) {
		if (packet instanceof IHasIp) {
			IHasIp p = (IHasIp) packet;
			for (InetAddress addr : addresses) {
				if (addr == null)
					continue;
				if (addr.equals(p.srcIp()) || addr.equals(p.dstIp()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the packet's source or destination MAC address matches one of
	 * the provided MAC addresses.
	 * 
	 * @param packet The packet to inspect
	 * @param macs   The set of MAC addresses to match against
	 * @return return true if source or destination IP is in the set; false
	 *         otherwise
	 */
	public static boolean mac(Packet packet, MacAddress... macs) {
		if (packet instanceof IHasMac) {
			IHasMac p = (IHasMac) packet;
			for (MacAddress m : macs) {
				if (m == null)
					continue;
				if (m.equals(p.srcMac()) || m.equals(p.dstMac()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether specified MAC address matches one of the provided MAC
	 * addresses.
	 * 
	 * @param mac1 The MAC address to inspect
	 * @param macs The set of MAC addresses to match against
	 * @return return true if source or destination IP is in the set; false
	 *         otherwise
	 */
	public static boolean mac(MacAddress mac1, MacAddress... macs) {
		if (mac1 != null) {
			for (MacAddress m : macs) {
				if (m == null)
					continue;
				if (m.equals(mac1) || m.equals(mac1))
					return true;
			}
		}
		return false;
	}

}
