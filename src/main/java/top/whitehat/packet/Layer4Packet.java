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

import java.net.InetAddress;


/**
 * A base class for packets of OSI Layer 4 protocols, 
 * 
 * such as TcpPacket and UdpPacket.​
 */
public abstract class Layer4Packet extends Packet implements IHasMac, IHasIpPort {

	public Layer4Packet() {
		super();
	}

	public Layer4Packet(byte[] data) {
		super(data);
	}
	
	/** get source InetAddress */
	public InetAddress srcIp() { 
		if (_parent instanceof IHasIp) {
			return ((IHasIp)_parent).srcIp();
		}
		return null;
	}
	
	/** put source InetAddress */
	public Layer4Packet srcIp(InetAddress addr) {
		if (_parent instanceof IHasIp) {
			((IHasIp)_parent).srcIp(addr);
		}
		return this;
	}
	
	/** get destination InetAddress */
	public InetAddress dstIp() {
		if (_parent instanceof IHasIp) {
			return ((IHasIp)_parent).dstIp();
		}
		return null;
	}
	
	/** put destination InetAddress */
	public Layer4Packet dstIp(InetAddress addr) {
		if (_parent instanceof IHasIp) {
			((IHasIp)_parent).dstIp(addr);
		}
		return this;
	}
		
	/** get source MAC address */
	public MacAddress srcMac() {
		if (_parent instanceof IHasMac) {
			return ((IHasMac)_parent).srcMac();
		}
		return null;
	}
	
	/** set source MAC address */
	public Layer4Packet srcMac(MacAddress addr) {	
		if (_parent instanceof IHasMac) {
			((IHasMac)_parent).srcMac(addr);
		}
		return this;
	}
	
	/** get destination MAC address */
	public MacAddress dstMac() {		
		if (_parent instanceof IHasMac) {
			return ((IHasMac)_parent).dstMac();
		}
		return null;
	}
	
	/** set destination MAC address */
	public Layer4Packet dstMac(MacAddress addr) {
		if (_parent instanceof IHasMac) {
			((IHasMac)_parent).srcMac(addr);
		}
		return this;
	}
	
	/** get source port */
	public int srcPort() {
		if (_parent instanceof IHasIpPort) {
			return ((IHasIpPort)_parent).srcPort();
		}
		return 0;
	}

	/** set source port */
	public Layer4Packet srcPort(int value) {
		if (_parent instanceof IHasIpPort) {
			((IHasIpPort)_parent).srcPort(value);
		}
		return this;
	}

	/** get destination port */
	public int dstPort() {
		if (_parent instanceof IHasIpPort) {
			return ((IHasIpPort)_parent).dstPort();
		}
		return 0;
	}

	/** set destination port */
	public Layer4Packet dstPort(int value) {
		if (_parent instanceof IHasIpPort) {
			((IHasIpPort)_parent).dstPort(value);
		}
		return this;
	}	
	
}
