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
package top.whitehat.dns;

/**
 * DNS query type
 * https://www.rfc-editor.org/rfc/rfc1035
 * 
 */
public enum DnsQueryType {
	
	/** Query Type: query IPv4 address */
	A(1),

	/** Query Type: an authoritative name server */
	NS(2),

	/** Query Type: Mail destination */
	MD(3),

	/** Query Type: Mail forwarder */
	MF(4),

	/** Query Type: the canonical name for an alias */
	CNAME(5),

	/** Query Type: marks the start of a zone of authority */
	SOA(6),

	/** Query Type: Mailbox domain name */
	MB(7),

	/** Query Type: Mail group member */
	MG(8),

	/** Query Type: Mail rename domain name */
	MR(9),

	/** Query Type: Null RR */
	NULL(10),

	/** Query Type: a well known service description */
	WKS(11),

	/** Query Type: a domain name pointer */
	PTR(12),

	/** Query Type: host information */
	HINFO(13),

	/** Query Type: Mailbox or mail list information */
	MINFO(14),

	/** Query Type: mail exchange */
	MX(15),

	/** Query Type: text strings */
	TXT(16),

	/** Query Type: Security signature */
	SIG(24),

	/** Query Type: Security key */
	KEY(25),

	/** Query Type: IPv6 address, 0x1C */
	AAAA(28),

	/** Query Type: Location Information */
	LOC(29),

	/** Query Type: Next Domain */
	NXT(30),
	
	/** Query Type: NetBIOS general Name Service */
	NetBIOS(32),
	
	/** Query Type: NetBIOS NODE STATUS */
	NetBIOS_STAT(33),


	/** Query Type: OPT */
	OPT(41),
	
	/** Query Type: HTTPS, defined in RFC 9460 */
	HTTPS(65),   

	/** Query Type: TKEY */
	TKEY(249),

	/** Query Type: TSIG */
	TSIG(250),
	
	UNKNOWN(0);
	

	private int value = 0;

	DnsQueryType(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static DnsQueryType of(int value) {
		for (DnsQueryType q : DnsQueryType.values())
			if (q.getValue() == value)
				return q;
		
		return UNKNOWN;
	}
	
	public static DnsQueryType of(String name) {
		try {
			return DnsQueryType.valueOf(name);
		} catch (Exception e) {
			return UNKNOWN;
		}
	}
	
	public static String name(int value) {
		return DnsQueryType.of(value).toString();
	}
}
