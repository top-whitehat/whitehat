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
package top.whitehat.tools;

import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import top.whitehat.NetCard;
import top.whitehat.NetCardAddress;
import top.whitehat.NetEventArgs;
import top.whitehat.NetEventListener;
import top.whitehat.NetUtil;
import top.whitehat.packet.ArpPacket;
import top.whitehat.packet.IHasIp;
import top.whitehat.packet.IcmpPacket;
import top.whitehat.packet.Packet;

public class ScanIp extends ScanBase {
	
	/** use ping to detect ip existence */
	public static final int PING = 1;
	
	/** do not use ping to detect ip existence */
	public static final int NO_PING = 2;
	
	/** wait time for response,  in millisecond */
	public int waitTime = 800;
	
	/** A map store InetAddress->existence */
	public LinkedHashMap<InetAddress, Object> ipMap = new LinkedHashMap<InetAddress, Object>();
	
	public ScanIp() {
		super();
	}
	
	public ScanIp(NetCard netCard) {
		super(netCard);
	}
	
	/** Get the wait time for response,  in millisecond */
	public int waitTime() {
		return waitTime;
	}

	/** Set the wait time for response,  in millisecond */
	public ScanIp waitTime(int value) {
		waitTime = value;
		return this;
	}
	
	/** clear */
	public ScanIp clear() {
		super.clear();
		ipMap.clear();
		return this;
	}
	
	/** Check the results: whether specified IP address exists */
	public boolean isExists(InetAddress addr) {
		return ipMap.getOrDefault(addr, null) != null;
	}
	
	/** Detect one Ip
	 * 
	 * @param dstAddress The destination IP address to detect
	 * @param srcAddress The source IP address of the network card
	 * @param srcMac     The source MAC address of the network card
	 */
	protected void detectIp(InetAddress dstAddress) {
		int dataSize = 32;
		int ttl = 200;
		int id = 1;
		
		switch (retryCount) {
		case 0:
			if (hasOption(PING)) {
				// send ICMP echo request (ping)
				netCard.sendPacket(IcmpPacket.request(dstAddress, srcAddress, id, retryCount, dataSize, ttl));
			} else if (netCard.isInSameSubnet(dstAddress)) {
				// send ARP request
				netCard.sendPacket(ArpPacket.request(srcAddress, srcMac, dstAddress));
			} else {
				// send ICMP timestamp request
				netCard.sendPacket(IcmpPacket.timestampRequest(dstAddress, srcAddress, id, retryCount, Instant.now()));
			}
			break;
		default:
			if (hasOption(PING)) {
				// send ICMP echo request (ping)
				netCard.sendPacket(IcmpPacket.request(dstAddress, srcAddress, id, retryCount, dataSize, ttl));
				
			} else {
				if (hasOption(NO_PING)) {
					// send ICMP echo request (ping)
					netCard.sendPacket(IcmpPacket.timestampRequest(dstAddress, srcAddress, id, retryCount, Instant.now()));
				} else {
					// send ICMP timestamp request
					netCard.sendPacket(IcmpPacket.request(dstAddress, srcAddress, id, retryCount, dataSize, ttl));
				}
			}
			break;
		}
	}
	

	/**
	 * Scan the Ip range specified by ip and mask
	 * 
	 * @param ipAndMask ip/mask string, such as: 192.168.0.1/24
	 * 
	 * @return this
	 */
	public ScanBase scan(String ipAndMask) {
		return scan(NetCardAddress.getByName(ipAndMask));
	}

	/**
	 * Scan Ip range specified by InetAddress and the network prefix length
	 * 
	 * @param address             The InetAddress
	 * @param networkPrefixLength The network prefix length
	 * 
	 * @return this
	 */
	public ScanBase scan(InetAddress address, int networkPrefixLength) {
		return scan(new NetCardAddress(address, networkPrefixLength));
	}

	/**
	 * Scan specified Ip range
	 * 
	 * @param ipRange
	 * @return this
	 */
	public ScanBase scan(NetCardAddress ipRange) {
		// where network card starts
		netCard.onStart(e -> {
			// scan once in a Thread
			NetUtil.runThread(() -> {
				if (!canceled)
					doScan(ipRange);
			});
		});

		// start the card
		canceled = false;
		netCard.start();

		return this;
	}

	/** Scan specified Ip address array */
	public ScanBase scan(InetAddress[] addresses) {
		// where network card starts
		netCard.onStart(e -> {
			// scan in a Thread
			NetUtil.runThread(() -> {
				if (!canceled)
					doScan(addresses);
			});
		});

		// start the card
		canceled = false;
		netCard.start();

		return this;
	}

	/** Scan specified Ip address List */
	public ScanBase scan(List<InetAddress> addresses) {
		return scan(addresses.toArray(new InetAddress[addresses.size()]));
	}

	/** Scan specified Ip address array */
	protected ScanBase doScan(InetAddress[] addresses) {
		if (canceled)
			return this;

		long count = addresses.length;
		long total = (retryTimes + 1) * count; // total ip count
		long startTime = System.currentTimeMillis();
		int i = 0;

		for (InetAddress dstAddress : addresses) {
			if (canceled)
				break;

			// if the value of the address is not null (already exist)
			if (ipMap.getOrDefault(dstAddress, null) != null) {
				continue;
			}

			// if the address is a new address
			if (!ipMap.containsKey(dstAddress)) {
				// put it into ipMap
				synchronized (ipMap) {
					ipMap.put(dstAddress, null);
				}
			}

			// detect the address
			detectIp(dstAddress);

			// report progress
			doProgress(i, count, startTime);

			i++;
		}

		// increase retry count
		retryCount++;

		if (!canceled) {
			if (retryCount < retryTimes) {
				// if retry is in need, retry after a while
				NetUtil.setTimeout(() -> {
					doScan(addresses);
				}, waitTime);

			} else {
				// there is no more retry, stop() after a while
				NetUtil.setTimeout(() -> {
					triggerOnProgress(total, total);
					netCard.stop();
				}, waitTime);
			}
		}

		return this;
	}

	/** Scan specified Ip range */
	protected ScanBase doScan(NetCardAddress ipRange) {
		if (canceled)
			return this;

		// prepare parameters
		int prefixLen = ipRange.getNetworkPrefixLength();
		long rangeIpNumber = NetUtil.ipToLong(ipRange.getAddress());
		long startIpNumber = rangeIpNumber & (0xFFFFFFFFL << (32 - prefixLen));
		long count = 0xFFFFFFFFL >> prefixLen; // ip count
		long total = (retryTimes + 1) * count; // total ip count
		long startTime = System.currentTimeMillis();

		InetAddress dstAddress;

		// iterate all Ips
		for (int i = 0; i < count + 1; i++) {
			if (canceled)
				break;

			// get a destination address by ipNumber
			long ipNumber = startIpNumber + i;
			dstAddress = NetUtil.longToIp(ipNumber);

			// skip loop back addresses
			if (dstAddress.isLoopbackAddress() || dstAddress.isMulticastAddress()) {
				continue;
			}

			// if the value of the address is not null (already exist)
			if (ipMap.getOrDefault(dstAddress, null) != null) {
				continue;
			}

			// if the address is a new address
			if (!ipMap.containsKey(dstAddress)) {
				// put it into ipMap
				synchronized (ipMap) {
					ipMap.put(dstAddress, null);
				}
			}

			// detect the address
			detectIp(dstAddress);

			// report progress
			doProgress(i, count, startTime);
		}

		// increase retry count
		retryCount++;

		if (!canceled) {
			if (retryCount < retryTimes) {
				// if retry is in need, retry after a while
				NetUtil.setTimeout(() -> {
					doScan(ipRange);
				}, waitTime);

			} else {
				// there is no more retry, stop() after a while
				NetUtil.setTimeout(() -> {
					triggerOnProgress(total, total);
					netCard.stop();
				}, waitTime);
			}
		}

		return this;
	}

	/** Implement onPacket() of PacketListener */
	@Override
	public void onPacket(Packet packet) {
		if (packet instanceof IHasIp) {
			IHasIp ipkt = (IHasIp) packet;

			InetAddress src = ipkt.srcIp();
			if (ipMap.containsKey(src)) {
				triggerOnFind(src, true);
			}
		}
	}

	/** Trigger onFind event */
	protected void triggerOnFind(InetAddress addr, Object value) {
		if (ipMap.containsKey(addr)) {
			// if the value of the address is not null (already exist)
			if (ipMap.getOrDefault(addr, null) != null)
				return;

			// put the address into ipMap
			synchronized (ipMap) {
				ipMap.put(addr, value);
			}

			// trigger onFind event
			for (NetEventListener l : findListeners) {
				l.onEvent(new NetEventArgs(this, addr, null));
			}
		}
	}

	/** Return List of exist IP addresses  */
	public List<InetAddress> exists() {
		List<InetAddress> list = new ArrayList<InetAddress>();
		for (Map.Entry<InetAddress, Object> entry : ipMap.entrySet()) {
			if (entry.getValue() != null)
				list.add(entry.getKey());
		}

		return list;
	}

	/** Return List of not-exist IP addresses */
	public InetAddress[] notExists() {
		List<InetAddress> list = new ArrayList<InetAddress>();
		for (Map.Entry<InetAddress, Object> entry : ipMap.entrySet()) {
			if (entry.getValue() == null)
				list.add(entry.getKey());
		}

		return list.toArray(new InetAddress[list.size()]);
	}
	
}
