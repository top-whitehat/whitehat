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


/** Ethernet implement a Java class that can read/write Ethernet packet
 * 
 * 
 * <pre>
 ​​Ethernet II packet format
┌───────────────┬──────────────┬───────────┬─────────────────┬───────────┐
│    dst MAC    │ source MAC   │ TYPE/LEN  │    payload      │ checksum  │
│  (6 bytes)    │  (6 bytes)   │ (2 bytes) │ (46-1500 bytes) │ (4 bytes) │
└───────────────┴──────────────┴───────────┴─────────────────┴───────────┘
┌───────────────┬──────────────┬──────────┬───────────┬─────────────────┬───────────┐
│    dst MAC    │ source MAC   │ VLAN_TAG │ TYPE/LEN  │    payload      │ checksum  │
│  (6 bytes)    │  (6 bytes)   │ (4 bytes)│ (2 bytes) │ (46-1500 bytes) │ (4 bytes) │
└───────────────┴──────────────┴──────────┴───────────┴─────────────────┴───────────┘

destination MAC    : byte  0,  6 bytes  , value=6 bytes MAC address
source MAC         : byte  6,  6 bytes  , value=6 bytes MAC address
VLAN_TAG(optional) : byte 12,  4 bytes  , 0x8100 + 2bytes  VLANTagged
TYPE/LEN           : byte 12,  2 bytes  , is ETHERNET when larger than 0x0600, IPV4=0x0800, IPV6=0x86DD, ARP=0x0806
payload            : byte 14,  46-1500 bytes, should padding if payload length less than 46
checksum           : byte -2,  4  bytes     , CRC-32
 * 
 * </pre>
 */ 
public class Ethernet extends Packet {
	// types 
	/** EtherNet : when type > 0x600 */
	public static final int ETHERNET = 0x0600; 
	
	/** Address Resolution Protocol */
	public static final int ARP = 0x0806;
	
	/** Reverse Address Resolution Protocol */
	public static final int RARP = 0x8035;
	
	/** Internet Protocol version 4 */
	public static final int IPV4 = 0x0800; 
	
	/** Internet Protocol version 6 */
	public static final int IPV6 = 0x86DD;
	
	/** Apple talk resolution protocol */
	public static final int AARP = 0x80F3;

	/** Apple talk Protocol */
	public static final int APPLE_TALK = 0x809B;
	
	/** VLAN: IEEE 802.1Q ​​VLAN-tagged frame */
	public static final int VLAN_TAGGED_FRAME = 0x8100;
	
	/** SNMP */
	public static final int SNMP = 0x814C; 
	
	/** IEEE 802.3x Ethernet Flow Control */
	public static final int FLOW_CONTROL = 0x8808;
	
	/** MPLS (unicast) */
	public static final int MPLS = 0x8847;
	
	/** MPLS (multicast) */
	public static final int MPLS_MULTICAST = 0x8848;
	
	/** PPP over Ethernet (Session Stage) */
	public static final int PPPOE = 0x8864;
	
	/** PPP over Ethernet (Discovery Stage) */
	public static final int PPPOE_D = 0x8863;
	
	/** 802.1ad Provider Bridging */
	public static final int Q_IN_Q = 0x88A8; 
	
	/** header size in bytes */
    public  static final int HEADER_SIZE = 14;
    
    /** minimum payload length in bytes */
	public static final int MIN_PAYLOAD_LENGTH = 46;
    
    /* offset of each fields */
    private static final int OFFSET_DST_MAC = 0;
    private static final int OFFSET_SRC_MAC = 6;
    private static final int OFFSET_TYPE = 12;
    
    /**  Offset for VLAN Tagged Frame */
    protected int vlanTaggedOffset = 0;  // 0 for Non-VLAN-Tagged, 4 for VLAN-Tagged    
    protected int vlanPRI = 0;   // 3-bit, Priority in IEEE 802.1Q ​​VLAN-tagged frame
    protected int vlanCFI = 0;   // 1-bit, Canonical Format Indicator
    protected int vlanId = 0;    // 12-bit, VLAN Identifier
    
    public static Packet wrap(byte[] pktData) {
    	return new Ethernet(pktData).child();
    }

    /** create a Ethernet packet with specified payload length */
	public static Ethernet create(int type, MacAddress srcMac, MacAddress dstMac, int payloadLength) {
		int len = payloadLength < MIN_PAYLOAD_LENGTH ? MIN_PAYLOAD_LENGTH : payloadLength;
		Ethernet ether = new Ethernet().init(HEADER_SIZE + len);
		ether.type(type);
		ether.srcMac(srcMac == null ? MacAddress.NULL : srcMac);
		ether.dstMac(dstMac == null ? MacAddress.NULL : dstMac);			
		return ether;
	}
		
	/** create a Ethernet packet  with specified payload */
	public static Ethernet create(int type, MacAddress srcMac, MacAddress dstMac, byte[] payload) {
		Ethernet ether = create(type, srcMac, dstMac, payload.length);
		ether.payload(payload);		
		return ether;
	}
	
	/** create a Ethernet packet that hold the specified packet */
	public static Ethernet create(Packet p, MacAddress srcMac, MacAddress dstMac) {
		int type = p instanceof ArpPacket ? Ethernet.ARP : Ethernet.IPV4; //
		return create(type, srcMac, dstMac, p.array());
	}
	
	/** Create an empty Ethernet packet */
	public Ethernet() {
		super();
	}
	
	/** Create an Ethernet packet by reading the data */
	public Ethernet(byte[] data) {
		super(data);
		readVlanTaggedFrame();
	}
    
	@Override
	public Ethernet init(int nBytes) {
		this.capacity(nBytes);
		this.srcMac(MacAddress.NULL).dstMac(MacAddress.NULL).type(IPV4);
		return this;
	}
	
    /** Returns the header length in bytes. */
	public int headerLength() {
		return HEADER_SIZE + vlanTaggedOffset;
	}

	/** Returns the header length in bytes. */
	public Packet headerLength(int len) {
		super.headerLength(len);
		if (len != HEADER_SIZE)
			throw new PacketException(getClassName() + " header length should be " + HEADER_SIZE);
		return this;
	}
	
	/** Return length of payload */
	@Override
	public int payloadLength() {
		return array() == null ? -1 : arrayLength() - arrayOffset() - headerLength();
	}

    /** Get destination MAC address */
    public MacAddress dstMac() {
		return new MacAddress(getBytes(OFFSET_DST_MAC, 6));
	}

    /** Set destination MAC address */
    public Ethernet dstMac(MacAddress addr) {
		put(OFFSET_DST_MAC, addr.array());
		return this;
	}

    /** Get source MAC address */
	public MacAddress srcMac() {
		return new MacAddress(getBytes(OFFSET_SRC_MAC, 6));
	}

    /** Set source MAC address */
    public Ethernet srcMac(MacAddress addr) {
    	put(OFFSET_SRC_MAC, addr.array());
		return this;
	}
    
    /** Read IEEE 802.1Q vLan-tagged parameters */
    protected void readVlanTaggedFrame() {
    	int val = getUInt16(OFFSET_TYPE);
    	if (val == VLAN_TAGGED_FRAME) {
    		vlanTaggedOffset = 4;
    		val = getUInt16(vlanTaggedOffset + OFFSET_TYPE);
    		vlanPRI = (val & (0b111 << 13)) >> 13;   // 3-bit, Priority in IEEE 802.1Q ​​VLAN-tagged frame
    		vlanCFI = (val & (0b1 << 12)) >> 12;   // 1-bit, Canonical Format Indicator
    		vlanId = val & 0xFFF;    // 12-bit, VLAN Identifier
    	} else {
    		vlanTaggedOffset = 0;
    		vlanPRI = 0;   // 3-bit, Priority in IEEE 802.1Q ​​VLAN-tagged frame
    		vlanCFI = 0;   // 1-bit, Canonical Format Indicator
    		vlanId = 0;    // 12-bit, VLAN Identifier
    	}
    }

    /** Get value of type */ 
    public int type() {
        int val = getUInt16(vlanTaggedOffset + OFFSET_TYPE);
        if (val == VLAN_TAGGED_FRAME && vlanTaggedOffset == 0) {
        	readVlanTaggedFrame();
        }
        return val;
    }

    /** Set value of type */ 
    public Ethernet type(int value) {
        putUInt16(vlanTaggedOffset + OFFSET_TYPE, value);
        return this;
    }
    
    /** Get id of VLAN */ 
    public int vlan() {
		return this.vlanId;
	}
    
    /** Set id of VLAN */ 
    public Ethernet vlan(int id) {
		this.vlanId = id;
		vlanTaggedOffset = 4;
		return this;
	}

    public String toString() {
		return "Ethernet,  Src: " + srcMac() + ", Dst: " + dstMac() + ", Type: " +  String.format("0x04X", type());
	}

	/** Set payload */
	public Ethernet payload(byte[] data) {
		int len = data.length;
		put(headerLength(), data);
		// if data length is less than the minimum payload length, padding zero.
		if (len < MIN_PAYLOAD_LENGTH) {
			int offset = headerLength() + len;
			for (int i = 0; i < MIN_PAYLOAD_LENGTH - len; i++) {
				putByte(offset + i + vlanTaggedOffset, (byte) 0);
			}
		}
		return this;
	}
	

	
	
	/** Return child packet that exists in the payload */
	@Override
	public Packet child() {
		int t = type();
		switch (t) {
		case IPV4:
			return child(IpV4Packet.class).child();
		case IPV6:
			return child(IpV6Packet.class).child();
		case ARP:
			return child(ArpPacket.class);
		default:
			throw new RuntimeException("unknow type " + String.format("0x04X", type()));
		}
	}

	/** Return IP packet that exists in the payload */
	public IpPacket getIpPacket() {
		if (type() == IPV4 || type() == IPV6)
			return child(IpPacket.class);
		return null;
	}
	
	
	
}

