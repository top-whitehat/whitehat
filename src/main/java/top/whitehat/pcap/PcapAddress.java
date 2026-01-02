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

import static top.whitehat.pcap.PcapNetworkInterface.*;

import java.net.InetAddress;

import top.whitehat.pcap.PcapLib.pcap_addr;
import top.whitehat.pcap.PcapLib.sockaddr;

/**
 * This class represents a libpcap Network Interface address. <br>
 * In short it's an IP address, a subnet mask and a broadcast address and a
 * destination address(for point-to-point interface)
 * 
 */
public abstract class PcapAddress {

	private InetAddress address;

	private InetAddress netmask;

	private InetAddress broadcast;

	private InetAddress destination; // for point-to-point interface

	public PcapAddress() {
		
	}
	
	public PcapAddress(pcap_addr pcapAddr, short saFamily) {
		if (pcapAddr == null) {
			throw new NullPointerException();
		}

		if (pcapAddr.addr != null && pcapAddr.addr.getSaFamily() != AF_UNSPECIFIED) {
			if (pcapAddr.addr.getSaFamily() != saFamily) {
				this.address = null; // addr saFamily is mismatch
			} else {
				this.address = ntoInetAddress(pcapAddr.addr);
			}
		} else {
			this.address = null;
		}

		if (pcapAddr.netmask != null && pcapAddr.netmask.getSaFamily() != AF_UNSPECIFIED) {
			if (pcapAddr.netmask.getSaFamily() != saFamily) {
				this.netmask = null; // netmask saFamily is mismatch
			} else {
				this.netmask = ntoInetAddress(pcapAddr.netmask);
			}
		} else {
			this.netmask = null;
		}

		if (pcapAddr.broadaddr != null && pcapAddr.broadaddr.getSaFamily() != AF_UNSPECIFIED) {
			if (pcapAddr.broadaddr.getSaFamily() != saFamily) {
				this.broadcast = null; // netmask saFamily is mismatch
			} else {
				this.broadcast = ntoInetAddress(pcapAddr.broadaddr);
			}
		} else {
			this.broadcast = null;
		}

		if (pcapAddr.dstaddr != null && pcapAddr.dstaddr.getSaFamily() != AF_UNSPECIFIED) {
			if (pcapAddr.dstaddr.getSaFamily() != saFamily) {
				// dstaddr saFamily is incorrect
				this.destination = null;
			} else {
				this.destination = ntoInetAddress(pcapAddr.dstaddr);
			}
		} else {
			this.destination = null;
		}
	}

	public PcapAddress(InetAddress address, InetAddress netmask, InetAddress broadcast, InetAddress destination) {
		this.address = address;
		this.netmask = netmask;
		this.broadcast = broadcast;
		this.destination = destination;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress addr) {
		this.address = addr;
	}
	
	public InetAddress getNetmask() {
		return netmask;
	}

	public void setNetmask(InetAddress netmask) {
		this.netmask = netmask;
	}
	
	public InetAddress getBroadcast() {
		return broadcast;
	}

	public void setBroadcast(InetAddress broadcast) {
		this.broadcast = broadcast;
	}
	
	public InetAddress getDestination() {
		return destination;
	}

	public void setDestination(InetAddress dest) {
		this.destination = dest;
	}
	
	public short getNetworkPrefixLength() {
		if (netmask == null)
			return -1;

		// calculate NetworkPrefixLengt by netmask
		byte[] maskBytes = netmask.getAddress();
		short prefixLength = 0;
		boolean zeroIsMet = false; // whether zero bit is met

		// iterate each byte
		for (byte b : maskBytes) {
			int unsignedByte = b & 0xFF;
			// iterate each bit of a byte
			for (int i = 7; i >= 0; i--) {
				int bit = (unsignedByte >> i) & 1;
				if (!zeroIsMet) {
					if (bit == 1) prefixLength++;
					else zeroIsMet = true;
				} else {
					if (bit == 1) return -1;
				}
			}
		}

		return zeroIsMet ? prefixLength : -1;
	}

	protected abstract InetAddress ntoInetAddress(sockaddr sa);

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("address: [").append(address) //
				.append("] netmask: [").append(netmask) //
				.append("] broadcastAddr: [").append(broadcast) //
				.append("] destination [").append(destination) //
				.append("]");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		PcapAddress other = (PcapAddress) obj;

		if (address == null) {
			if (other.address != null)
				return false;

		} else if (!address.equals(other.address))
			return false;

		if (broadcast == null) {
			if (other.broadcast != null)
				return false;

		} else if (!broadcast.equals(other.broadcast))
			return false;

		if (destination == null) {
			if (other.destination != null)
				return false;

		} else if (!destination.equals(other.destination))
			return false;

		if (netmask == null) {
			if (other.netmask != null)
				return false;

		} else if (!netmask.equals(other.netmask))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((broadcast == null) ? 0 : broadcast.hashCode());
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((netmask == null) ? 0 : netmask.hashCode());
		return result;
	}

}
