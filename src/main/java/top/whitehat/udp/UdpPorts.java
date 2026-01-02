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
package top.whitehat.udp;

import java.util.HashMap;
import java.util.Map;

import top.whitehat.packet.PacketUtil;

/**
 * UDP ports <br>
 * 
 * <a href="https://whatportis.com/">SEE: port information</a>
 */
public class UdpPorts {

	/** Login Host Protocol (TACACS) */
	public static final int TACACS = 49;

	/** Domain Name Server */
	public static final int DNS = 53;

	/** DHCP */
	public static final int DHCP = 67;

	/** Bootstrap Protocol */
	public static final int BOOTP = 68;

	/** Trivial File Transfer */
	public static final int TFTP = 69;

	/** Network Time Protocol */
	public static final int NTP = 123;

	/** SNMP */
	public static final int SNMP = 161;

	/** SNMP Trap */
	public static final int SNMP_TRAP = 162;

	/** X Display Manager Control Protocol */
	public static final int XDMCP = 177;

	/** Internet Relay Chat Protocol */
	public static final int IRC = 194;

	/** isakmp */
	public static final int ISAKMP = 500;

	/** syslog */
	public static final int SYSLOG = 514;

	/** RIP */
	public static final int RIP = 520;

	/** RIPNG(Ipv6) */
	public static final int RIPNG = 521;

	/** RMONITOR */
	public static final int RMONITOR = 560;

	/** L2TP */
	public static final int L2TP = 1701;

	/** H323GATESTAT */
	public static final int H323GATESTAT = 1719;

	/** STEAM */
	public static final int STEAM = 1725;

	/** HRSP */
	public static final int HRSP = 1985;

	/** RTP */
	public static final int RTP = 5004;

	/** Real Time Streaming Protocol (RTSP) */
	public static final int RTSP = 5005;

	/** TEAMSPEAK */
	public static final int TEAMSPEAK = 8767;

	public static Map<String, Integer> name2Value = new HashMap<String, Integer>();

	public static Map<Integer, String> value2Name = new HashMap<Integer, String>();

	static {
		PacketUtil.listFields(UdpPorts.class, name2Value, value2Name);
	}

	/** return protocol name by protocol int */
	public static String getName(int value) {
		return value2Name.getOrDefault(value, "");
	}

	/** return protocol int by protocol name */
	public static int getValue(String name) {
		return name2Value.getOrDefault(name.toUpperCase(), 0);
	}

}
