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

import static top.whitehat.dns.DnsQueryType.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import top.whitehat.NetUtil;

/**
 * DNS request
 */
public class DnsRequest {

	/** client ip address */
	public InetSocketAddress socketAddress = null;
	
	private DnsPacket packet;
	
	/** Constructor */
	public DnsRequest(DnsPacket packet) {
		this.packet = packet;
		this.socketAddress = new InetSocketAddress(packet.srcIp(), packet.srcPort());
	}
	
	/** Constructor */
	public DnsRequest(InetSocketAddress address, byte[] buffer) {
		this.packet = new DnsPacket(buffer);
		this.socketAddress = address;
	}
	
	public DnsPacket getDnsPacket() {
		return this.packet;
	}

	/** get client socket socket address */
	public InetSocketAddress getSocketAddress() {
		return this.socketAddress;
	}
	
	/** get client address */
	public InetAddress srcIp() {
		return this.socketAddress.getAddress();
	}

	/** get client ports */
	public int srcPort() {
		return this.socketAddress.getPort();
	}

	
	/** whether this request is a command */
	public boolean isCommand() {
		return packet.queryName().indexOf("=") > 0;
	}

	/** whether this request should be filtered */
	public boolean needFilter() {
		int type = packet.queryType();
		return type == A.getValue() || type == AAAA.getValue()|| type == HTTPS.getValue()||  
				type == MX.getValue() || type == CNAME.getValue() || type == NS.getValue();
	}

	/** local address */
	private InetAddress localhost = NetUtil.toInetAddress("127.0.0.1");
	
	/** whether this request is from local */
	public boolean isFromLocal() { 
		return localhost.equals(socketAddress.getAddress()); 
	}
}
