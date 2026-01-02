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
package top.whitehat;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * This class represents a group of addresses for a Network Interface card. <br>
 * In short it's an IP address, a subnet mask and a broadcast address when the
 * address is an IPv4 one. <br>
 * An IP address and a network prefix length in the case of IPv6 address.
 *
 */
public class NetCardAddress {

	/** Minimum network prefix length */
	public static int MIN_NETWORK_PREFIX_LENGTH = 8;
	
	/**
	 * Parses a string in the format 'IP/prefixLength', for example: '192.168.100.1/8'.
	 * The IP can be IPv4 or IPv6, and the prefixLength is an integer.
	 *
	 * @param ipAndMask the input string, e.g. '192.168.100.1/8'
	 * @return NetCardAddress object
	 * @throws IllegalArgumentException if the format is invalid
	 */
	public static NetCardAddress getByName(String ipAndMask) {
		String[] sections;
		String ipStr;
		String maskStr="";	    
	    InetAddress addr;
	    int maskLength;	    
	    
	    // cut prefix "/"
	    if (ipAndMask.startsWith("/")) 
	    	ipAndMask= ipAndMask.substring(1);
	    
	    // split by "/"
		if (ipAndMask.contains("/")) {
			sections = ipAndMask.split("/");
			ipStr = sections[0];
			maskStr = sections[1];			
		} else {
			maskStr = "32";
			ipStr = ipAndMask;
		}
		
		// parse address
		int ipBytes = 4;
		try {
			addr = InetAddress.getByName(ipStr);
			ipBytes = addr.getAddress().length;
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid IP '" + ipStr + "'");
		}
		
		// parse maskLength
		try {
			maskLength = Integer.parseInt(maskStr);
			if (maskLength < MIN_NETWORK_PREFIX_LENGTH ) {
				throw new IllegalArgumentException("mask should not less than " + MIN_NETWORK_PREFIX_LENGTH);
			}
			if (maskLength > ipBytes * 8 ) {
				throw new IllegalArgumentException("mask should not more than " + (ipBytes * 8));
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid mask '" + maskStr + "'");
		}
		
		// create a NetCardAddress object 
		return new NetCardAddress(addr, maskLength, null);
	}
	
	/** InetAddress */
	private InetAddress _address;

	/** mask length */
	private int _maskLength = 0;
	
	/** Gateway InetAddress */
	private InetAddress _gateway;

	/** Constructor */
	public NetCardAddress() {

	}
	
	/** Constructor */
	public NetCardAddress(InetAddress address, int maskLength) {
		this(address, maskLength, null);
	}
	
	/** Constructor */
	public NetCardAddress(InetAddress address, int maskLength, InetAddress gateway) {
		this._address = address;
		this._maskLength = maskLength;
		this._gateway = gateway;
	}
	
	/** Get address	 */
	public InetAddress getAddress() {
		return _address;
	}

	/** Set address */
	public void setAddress(InetAddress addr) {
		this._address = addr;
	}
	
	/** Get Gateway	 */
	public InetAddress getGateway() {
		return _gateway;
	}

	/** Set Gateway */
	public void setGateway(InetAddress addr) {
		this._gateway = addr;
	}

	/**
	 * Returns the network prefix length for this address. This is also known as the
	 * subnet mask in the context of IPv4 addresses. Typical IPv4 values would be 8
	 * (255.0.0.0), 16 (255.255.0.0) or 24 (255.255.255.0).
	 * <p>
	 * Typical IPv6 values would be 128 (::1/128) or 10
	 * (fe80::203:baff:fe27:1243/10)
	 *
	 * @return a {@code int} representing the prefix length for the subnet of that
	 *         address.
	 */
	public int getNetworkPrefixLength() {
		return _maskLength;
	}
	
	/** set mask length */
	public void setNetworkPrefixLength(int maskLength) {
		this._maskLength = maskLength;
	}
	
	/**
	 * Returns an {@code InetAddress} for the broadcast address for this
	 * InterfaceAddress.
	 * <p>
	 * Only IPv4 networks have broadcast address therefore, in the case of an IPv6
	 * network, {@code null} will be returned.
	 *
	 * @return the {@code InetAddress} representing the broadcast address or
	 *         {@code null} if there is no broadcast address.
	 */
	public InetAddress getBroadcast() {
        if (!(_address instanceof Inet4Address)) return null;
        if (_maskLength < 0 || _maskLength > 32) return null;

        byte[] ipBytes = _address.getAddress(); // 4 bytes
        int mask = _maskLength == 0 ? 0 : 0xFFFFFFFF << (32 - _maskLength);
        int hostMask = ~mask; // host bits all set to 1

        // Keep the network bits, set all host bits to 1
        int netAsInt = ByteBuffer.wrap(ipBytes).getInt() & mask;
        int bcastAsInt = netAsInt | hostMask;

        byte[] bcastBytes = new byte[]{
                (byte) (bcastAsInt >> 24),
                (byte) (bcastAsInt >> 16),
                (byte) (bcastAsInt >> 8),
                (byte) bcastAsInt
        };
        
        try {
			return InetAddress.getByAddress(bcastBytes);
		} catch (UnknownHostException e) {
			return null;
		}
    }
	
	/** 
	 * Compares this object against the specified object. The result is {@code
	 * true} if and only if the argument is not {@code null} and it represents the
	 * same interface address as this object. <p> Two instances of {@code
	 * InterfaceAddress} represent the same address if the InetAddress, the prefix
	 * length and the broadcast are the same for both.
	 *
	 * @param obj the object to compare against.
	 * 
	 * @return {@code true} if the objects are the same; {@code false} otherwise.
	 * 
	 * @see java.net.InterfaceAddress#hashCode()
	 */
	public boolean equals(Object obj) {
		if (obj instanceof NetCardAddress) {
			NetCardAddress cmp = (NetCardAddress) obj;
			return Objects.equals(_address, cmp._address)
					&& _maskLength == cmp._maskLength;
		} else if (obj instanceof java.net.InterfaceAddress) {
			java.net.InterfaceAddress cmp = (java.net.InterfaceAddress) obj;
			return Objects.equals(_address, cmp.getAddress())
					&& _maskLength == cmp.getNetworkPrefixLength();
		}
		return false;
	}

	/**
	 * Returns a hash code for this Interface address.
	 *
	 * @return a hash code value for this Interface address.
	 */
	public int hashCode() {
		return _address.hashCode() + _maskLength;
	}
	
	/**
	 * Converts this Interface address to a {@code String}. The string returned is
	 * of the form: InetAddress / prefix length [ broadcast address ].
	 *
	 * @return a string representation of this Interface address.
	 */
	public String toString() {
		return (_address == null ? "null" : _address.getHostAddress()) + "/" + _maskLength;
	}

	public boolean isInSameSubnet(InetAddress addr) {
		return NetUtil.isInSameSubnet(getAddress(), addr, getNetworkPrefixLength());
	}
}
