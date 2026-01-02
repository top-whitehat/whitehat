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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 <h3>ARP packet format <h/3>
 <a href="https://www.rfc-editor.org/rfc/rfc826"> SEE: RFC826 </a>
 
 <pre>
 
 0               1               2               3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|        Hardware Type           |       Protocol Type          |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|Hardware Length |Protocol Length|          Operation           |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                         Sender Mac(SHA)  (6 byte)                             |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                         Sender IP(SPA) (4 byte)               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                         Target Mac(THA) (6 byte)                              |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                         Target IP(TPA)  (4 byte)              |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

Hardware Type   : byte  0, 2 byte       , value= 1(Ethernet) 
Protocol Type   : byte  2, 2 byte       , value= 0x0800(IPv4)
Hardware Length : byte  4, 1 byte       , ​​value= 6(Mac address length)
Protocol Length : byte  5, 1 byte       , value= 4(IPv4 address length)
Operation       : byte  6, 2 byte       , value= 1(Request) or 2(Reply)
Sender Mac      : byte  8, 6 byte       , Mac address of sender
Sender IP       : byte  14, 4 byte      , IP address of sender
Target Mac      : byte  18, 6 byte      , value=00:00:00:00:00:00 (unknown in request)
Target IP       : byte  24, 4 byte      , IP you're trying to resolve
 * </pre>
 */ 
public class ArpPacket extends Layer3Packet {

    public  static final int HEADER_SIZE = 28;
    private static final int OFFSET_HARDWARE_TYPE = 0;
    private static final int OFFSET_PROTOCOL = 2;
    private static final int OFFSET_HARDWARE_LENGTH = 4;
    private static final int OFFSET_PROTOCOL_LENGTH = 5;
    private static final int OFFSET_OPERATION = 6;
    private static final int OFFSET_SENDER_MAC = 8;
    private static final int OFFSET_SENDER_IP = 14;
    private static final int OFFSET_TARGET_MAC = 18;
    private static final int OFFSET_TARGET_IP = 24;
    
    /** ARP Request operation */
	public final static int ARP_REQUEST = 1;

	/** ARP Reply operation */
	public final static int ARP_REPLY = 2;
	
	
	/** Create a ARP request packet with e parameters
	 * 
	 * @param srcIp      Source IP address
	 * @param dstIp      Destination IP address
	 * @param srcMac     Source Mac Address
	 * @return ArpPacket
	 */
	public static ArpPacket requestEx(InetAddress srcIp, MacAddress srcMac, 
			InetAddress senderIp, MacAddress senderMac, 
			InetAddress dstIp, MacAddress dstMac, 
			InetAddress targetIp, MacAddress targetMac) 
	{
		ArpPacket ret = new ArpPacket().init();
		ret.operation(ARP_REQUEST);
		if (srcIp != null) ret.srcIp(srcIp);
		if (senderIp != null) ret.senderIp(senderIp);
		if (srcMac != null) ret.srcMac(srcMac);
		if (senderMac != null) ret.senderMac(senderMac);
		if (dstIp != null) ret.dstIp(dstIp);
		ret.dstMac(dstMac == null ? MacAddress.BROADCAST : dstMac);
		if (targetIp != null) ret.targetIp(targetIp);
		if (targetMac != null) ret.targetMac(targetMac);
		return ret;
	}
	
	/** Create a ARP request packet
	 * 
	 * @param srcIp      Source IP address
	 * @param srcMac     Source Mac Address
	 * @param dstIp      Destination IP address
	 * 
	 * @return ArpPacket
	 */
	public static ArpPacket request(InetAddress srcIp, MacAddress srcMac, InetAddress dstIp) {
		ArpPacket ret = new ArpPacket().init();
		ret.operation(ARP_REQUEST);
		if (srcIp != null) ret.srcIp(srcIp);
		if (srcIp != null) ret.senderIp(srcIp);
		if (srcMac != null) ret.srcMac(srcMac);
		if (srcMac != null) ret.senderMac(srcMac);
		if (dstIp != null) ret.dstIp(dstIp);
		if (dstIp != null) ret.targetIp(dstIp);
		ret.dstMac(MacAddress.BROADCAST);
		return ret;
	}
	
	/** Create a ARP reply packet
	 * 
	 * @param srcIp      Source IP address
	 * @param srcMac     Source Mac Address
	 * @param dstIp      Destination IP address
	 * @param dstMac     Destination Mac Address
	 * 
	 * @return ArpPacket
	 */
	public static ArpPacket reply(InetAddress srcIp, MacAddress srcMac, InetAddress dstIp, MacAddress dstMac) {
		ArpPacket ret = new ArpPacket().init();
		ret.operation(ARP_REPLY);
		if (srcIp != null) ret.srcIp(srcIp);
		if (srcIp != null) ret.senderIp(srcIp);
		if (srcMac != null) ret.srcMac(srcMac);
		if (srcMac != null) ret.senderMac(srcMac);
		
		if (dstIp != null) ret.dstIp(dstIp);
		if (dstIp != null) ret.targetIp(dstIp);
		ret.dstMac(dstMac == null ? MacAddress.BROADCAST : dstMac);
		ret.targetMac(dstMac == null ? MacAddress.BROADCAST : dstMac);
		return ret;
	}

	/** Create an empty ArpPacket */
    public ArpPacket() {
        super();
    }

    /** Create ArpPacket from byte array */
    public ArpPacket(byte[] data) {
        super(data);
    }

    /** Get the value of hardware type */ 
    public int hardwareType() {
        return getUInt16(OFFSET_HARDWARE_TYPE);
    }

    /** Set the value of hardware type */ 
    public ArpPacket hardwareType(int value) {
        putUInt16(OFFSET_HARDWARE_TYPE, value);
        return this;
    }

    /** Get the value of protocol */ 
    public int protocol() {
        return getUInt16(OFFSET_PROTOCOL);
    }

    /** Set the value of protocol */ 
    public ArpPacket protocol(int value) {
        putUInt16(OFFSET_PROTOCOL, value);
        return this;
    }

    /** Get the value of hardware length */ 
    public int hardwareLength() {
        return getUInt8(OFFSET_HARDWARE_LENGTH);
    }

    /** Set the value of hardware length */ 
    public ArpPacket hardwareLength(int value) {
        putUInt8(OFFSET_HARDWARE_LENGTH, value);
        return this;
    }

    /** Get the value of protocol length */ 
    public int protocolLength() {
        return getUInt8(OFFSET_PROTOCOL_LENGTH);
    }

    /** Set the value of protocol length */ 
    public ArpPacket protocolLength(int value) {
        putUInt8(OFFSET_PROTOCOL_LENGTH, value);
        return this;
    }

    /** Get the value of operation */ 
    public int operation() {
        return getUInt16(OFFSET_OPERATION);
    }

    /** Set the value of operation */ 
    public ArpPacket operation(int value) {
        putUInt16(OFFSET_OPERATION, value);
        return this;
    }
    
    /** Check whether the packet is ARP_REQUEST */
    public boolean isRequest() {
    	return operation() == ARP_REQUEST;
    }
    
    /** Check whether the packet is ARP_REPLY */
    public boolean isReply() {
    	return operation() == ARP_REPLY;
    }

    /** Get the value of sender Mac */ 
    public MacAddress senderMac() {
    	return new MacAddress(getBytes(OFFSET_SENDER_MAC, 6));
    }

    /** Set the value of sender Mac */ 
    public ArpPacket senderMac(MacAddress addr) {
    	put(OFFSET_SENDER_MAC, addr.array());
        return this;
    }

    /** Get the value of sender ip */ 
    public InetAddress senderIp() {
    	byte[] bs = getBytes(OFFSET_SENDER_IP, 4);
    	try {
			return Inet4Address.getByAddress(bs);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("unknown host");
		}
    }

    /** Set the value of sender ip */ 
    public ArpPacket senderIp(InetAddress addr) {
    	if (addr instanceof Inet4Address) {
    		putBytes(OFFSET_SENDER_IP, addr.getAddress());
		} else {
			throw new IllegalArgumentException("not a IPv4 address");
		}
		return this;
    }

    /** Get the value of target Mac */ 
    public MacAddress targetMac() {
    	return new MacAddress(getBytes(OFFSET_TARGET_MAC, 6));
    }

    /** Set the value of target Mac */ 
    public ArpPacket targetMac(MacAddress addr) {
        put(OFFSET_TARGET_MAC, addr.array());
        return this;
    }

    /** Get the value of target ip */ 
    public InetAddress targetIp() {
    	byte[] bs = getBytes(OFFSET_TARGET_IP, 4);
    	try {
			return Inet4Address.getByAddress(bs);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("unknown host");
		}
    }

    /** Set the value of target ip */ 
    public ArpPacket targetIp(InetAddress addr) {
    	if (addr instanceof Inet4Address) {
    		putBytes(OFFSET_TARGET_IP, addr.getAddress());
		} else {
			throw new IllegalArgumentException("not a IPv4 address");
		}
		return this;
    }

	@Override
	public InetAddress srcIp() {
		return senderIp();
	}

	@Override
	public ArpPacket srcIp(InetAddress addr) {
		return senderIp(addr);
	}

	@Override
	public InetAddress dstIp() {
		return targetIp();
	}

	@Override
	public ArpPacket dstIp(InetAddress addr) {
		return targetIp(addr);
	}

	/** Init packet data */
	public ArpPacket init() {
		this.init(HEADER_SIZE);
		this.hardwareType(1) // 1(Ethernet)
				.protocol(Ethernet.IPV4) // 0x0800(IPv4)
				.hardwareLength(6) // 6(Mac address length)
				.protocolLength(4) // 4(IPv4 address length)
				.operation(ARP_REQUEST) // 1(Request)
				.senderMac(MacAddress.NULL) // senderMac
				.senderIp(PacketUtil.ipLocalHost) // senderIp
				.targetMac(MacAddress.BROADCAST) // targetMAC
				.targetIp(PacketUtil.ipLocalHost); // targetIp
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClassName());
		
		if (isRequest()) {
			sb.append(", REQUEST");
			sb.append(", Sender: ").append(senderIp().getHostAddress());
			sb.append(", Target: ").append(targetIp().getHostAddress());
		} else if (isReply()) {
			sb.append(", REPLY  ");
			sb.append(", Sender: ").append(senderIp().getHostAddress());
			sb.append(", Sender Mac: ").append(senderMac());
			sb.append(", Target: ").append(targetIp().getHostAddress());
		} else {
			sb.append(", Protocol: ").append(protocol());
			sb.append(", Sender: ").append(senderIp().getHostAddress());
			sb.append(", Target: ").append(targetIp().getHostAddress());
		}
		
		return sb.toString();
	}
}

