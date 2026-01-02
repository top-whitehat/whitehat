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

import java.util.HashMap;
import java.util.Map;

import top.whitehat.packet.Packet;
import top.whitehat.packet.PacketUtil;

public class Protocols {

	/** ARP: Address Resolution Protocol */
	public final static int ARP = 0x0806; // code defined in Ethernet packet 
	
	/** RARP: Reverse Address Resolution Protocol */
	public final static int RARP = 0x8035; // code defined in Ethernet packet 
	
	/** Internet Protocol version 4 */
	public final static int IPV4 = 0x0800; // code defined in Ethernet packet  
	
	/** Internet Protocol version 6 */
	public final static int IPV6 = 0x86DD; // code defined in Ethernet packet 
	
	/** IP: Internet Protocol */
	public final static int IP = 0xFF;    // self defined
	
	/** Internet Control Message Protocol */
	public final static int ICMP = 1;    // code defined in IP packet 
	
	/** Internet Control Message Protocol in IPv6 */
	public final static int ICMPV6 = 58;  // code defined in IP packet 
	
	/** IP in IP */
	public final static int IP_IN_IP = 4;  // code defined in IP packet
	
	/** TCP */
	public final static int TCP = 6;  // code defined in IP packet 

	/** UDP */
	public final static int UDP = 17; // code defined in IP packet 
	
	/** L2TP: ​​Layer 2 Tunneling Protocol​​ */
	public final static int L2TP = 115; // code defined in IP packet
	
	private static Map<String, Integer> name2Value = new HashMap<String, Integer>();
	
	private static Map<Integer, String> value2Name = new HashMap<Integer, String>();
	
	static {
		PacketUtil.listFields(Packet.class, name2Value, value2Name);
	}
	
	/** return protocol name by protocol int */
	public static String getProtocolName(int protocol) {
		return value2Name.getOrDefault(protocol, "" + protocol);
	}
	
	/** return protocol int by protocol name */
	public static int getProtocolValue(String name) {
		return name2Value.getOrDefault(name, 0);
	}
	
}
