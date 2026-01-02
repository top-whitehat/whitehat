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
package top.whitehat.packet;


public class DataLinkType {
	
	// Data Link Type
	// SEE: https://github.com/the-tcpdump-group/libpcap/blob/master/pcap/bpf.h

	/** Data Link Type: Null, no link layer header, the first byte is protocol */
	public static final int NULL = 0;

	/** Data Link Type: Ethernet (10Mb, 100Mb, 1000Mb, and up): 1 */
	public static final int ETHERNET = 1;
	
	/** Data Link Type: CHAOS : 5 */
	public static final int CHAOS = 5; //

	/** Data Link Type: IEEE802 "Token Ring" : 6 */
	public static final int TOKEN_RING = 6; //

	/** Data Link Type: SLIP: 8 */
	public static final int SLIP = 8;

	/** Data Link Type: Point-to-point Protocol: 9 */
	public static final int PPP = 9;

	/** Data Link Type: FDDI: 10 */
	public static final int FDDI = 10;

	/** Data Link Type: RAW IP packet: 14 on OpenBSD, or 12 on the others. */
	public static final int RAW = 12;
	
	public static final int RAW_OPENBSD = 14; 

	
	/** Data Link Type: PPP over Ethernet = PPPoE): 50 */
	public static final int PPPOE = 50; 

	/** Data Link Type: IEEE 802.11 WIFI wireless: 105 */
	public static final int WIRELESS = 105;

	/** Data Link Type: loopback: 108, such as from localhost */
	public static final int LOOPBACK = 108;

	/** Data Link Type: Linux cooked-mode capture  = SLL): 113 */
	public static final int LINUX_SLL = 113;

	/**
	 * Data Link Type: Radiotap: 127 - Header for 802.11 plus a number of bits of
	 * link-layer information including radio information
	 */
	public static final int RADIO_TAP = 127; 

	/** Data Link Type: Raw IPv4 Packet without link layer header */
	public static final int RAW_IPV4 = 228; 

	/** Data Link Type: Raw IPv6 Packet without link layer header */
	public static final int RAW_IPV6 = 229; 

	/** Data Link Type: DOCSIS MAC frames: 143 */
	public static final int DOCSIS = 143;
	
	/** Data Link Type: Others */
	public static final int OTHERS = 255;



}
