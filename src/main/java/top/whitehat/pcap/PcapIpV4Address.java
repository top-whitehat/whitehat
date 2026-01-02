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

import java.net.Inet4Address;
import java.net.InetAddress;

import top.whitehat.pcap.PcapLib.pcap_addr;
import top.whitehat.pcap.PcapLib.sockaddr;
import top.whitehat.pcap.PcapLib.sockaddr_in;

public final class PcapIpV4Address extends PcapAddress {

	public PcapIpV4Address(pcap_addr pcapAddr, short saFamily) {
		super(pcapAddr, saFamily);
	}

	public PcapIpV4Address(InetAddress address, InetAddress netmask, InetAddress broadcast, InetAddress destination) {
		setAddress(address);
		setNetmask(netmask);
		setBroadcast(broadcast);
		setDestination(destination);
	}

	@Override
	protected Inet4Address ntoInetAddress(sockaddr sa) {
    sockaddr_in addr = new sockaddr_in(sa.getPointer());
		return PcapUtils.intoInetAddress(addr.sin_addr);
	}

	@Override
	public  Inet4Address getAddress() {
		return (Inet4Address)super.getAddress();
	}

	@Override
	public  Inet4Address getNetmask() {
		return (Inet4Address)super.getNetmask();
	}

	@Override
	public  Inet4Address getBroadcast() {
		return (Inet4Address)super.getBroadcast();
	}

	@Override
	public  Inet4Address getDestination() {
		return (Inet4Address)super.getDestination();
	}

}

