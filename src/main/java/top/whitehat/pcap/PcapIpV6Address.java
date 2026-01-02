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

import java.net.Inet6Address;

import top.whitehat.pcap.PcapLib.pcap_addr;
import top.whitehat.pcap.PcapLib.sockaddr;
import top.whitehat.pcap.PcapLib.sockaddr_in6;

public final class PcapIpV6Address extends PcapAddress {

	public PcapIpV6Address(pcap_addr pcapAddr, short saFamily) {
		super(pcapAddr, saFamily);
	}

	@Override
	protected Inet6Address ntoInetAddress(sockaddr sa) {
		sockaddr_in6 addr = new sockaddr_in6(sa.getPointer());
		return PcapUtils.ntoInetAddress(addr.sin6_addr);
	}

	@Override
	public Inet6Address getAddress() {
		return (Inet6Address) super.getAddress();
	}

	@Override
	public Inet6Address getNetmask() {
		return (Inet6Address) super.getNetmask();
	}

	@Override
	public Inet6Address getBroadcast() {
		return (Inet6Address) super.getBroadcast();
	}

	@Override
	public Inet6Address getDestination() {
		return (Inet6Address) super.getDestination();
	}

}
