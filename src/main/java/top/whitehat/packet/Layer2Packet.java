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


/**
 * A base class for packets of OSI Data Link Layer (Layer 2) protocols, 
 * 
 * such as EtherPacket and PppPacket.​
 */
public abstract class Layer2Packet extends Packet implements IHasMac {
	
	Layer2Packet() {
		super();
	}
	
	Layer2Packet(byte[] data) {
		super(data);
	}
	
	/** get source MAC address */
	public abstract MacAddress srcMac();
	
	/** set source MAC address */
	public abstract Layer2Packet srcMac(MacAddress addr);
	
	/** get destination MAC address */
	public abstract MacAddress dstMac();
	
	/** set destination MAC address */
	public abstract Layer2Packet dstMac(MacAddress addr);
	
	/** get packet type */
	public abstract int type();

	/** set packet type */
	public abstract Layer2Packet type(int t);
	
//	/** get IP packet */
//	@Override
//	public IpPacket getIPPacket() {
//		int t = type();
//		if (t == Packet.IPV4) {
//			return  child(IpV4Packet.class);
//			
//		} else if (t == Packet.IPV6) {
//			return child(IpV6Packet.class);
//			
//		}
//		return null;
//	}
//	
//	public static int detectType(Packet p) {		
//		IpPacket ipkt = p.getIPPacket();
//		if ( ipkt == null) {
////			if (p instanceof ARP)   //TODO
////				return Packet.ARP; 
////			else  // TODO
////				return -1;
//			return -1;
//		} else {
//			if (ipkt.version() == 4) 
//				return Packet.IPV4;
//			else if (ipkt.version() == 6) 
//				return Packet.IPV6;
//			else 
//				return -1;
//		}
//	}

}
