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
 * A base class for packets of OSI Layer 3 protocols, 
 * 
 * such as IpPacket and ArpPacket.​
 */
public abstract class Layer3Packet extends Packet implements IHasIp, IHasMac {
	
	/** constructor */
	public Layer3Packet() {
		super();
	}

	/** constructor */
	public Layer3Packet(byte[] buf) {
		super(buf);
	}

	/** get source InetAddress */
	public abstract InetAddress srcIp();

	/** put source InetAddress */
	public abstract Layer3Packet srcIp(InetAddress addr);

	/** get destination InetAddress */
	public abstract InetAddress dstIp();

	/** put destination InetAddress */
	public abstract Layer3Packet dstIp(InetAddress addr);

	/** get source MAC address */
	public MacAddress srcMac() {
		if (parent() instanceof Layer2Packet) {
			return ((Layer2Packet) parent()).srcMac();
		}
		return null;
	}

	/** set source MAC address */
	public Layer3Packet srcMac(MacAddress addr) {
		if (parent() instanceof Layer2Packet) {
			((Layer2Packet) parent()).srcMac(addr);
		}
		return this;
	}

	/** get destination MAC address */
	public MacAddress dstMac() {
		if (parent() instanceof Layer2Packet) {
			return ((Layer2Packet) _parent).dstMac();
		}
		return null;
	}

	/** set destination MAC address */
	public Layer3Packet dstMac(MacAddress addr) {
		if (_parent instanceof Layer2Packet) {
			((Layer2Packet) _parent).dstMac(addr);
		}
		return this;
	}
	
	
}
