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
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import top.whitehat.NetCard;
import top.whitehat.NetEventArgs;
import top.whitehat.NetEventListener;
import top.whitehat.NetUtil;
import top.whitehat.packet.Packet;
import top.whitehat.packet.TcpPacket;
import top.whitehat.util.ArrayUtil;

public class ScanPort extends ScanBase {

	/** Indicates use random source port when detecting */
	public static final int RANDOM_PORT = 1;

	/** source port when detecting */
	protected int srcPort = 5354;

	/** A map store InetAddress->existence */
	public HashMap<InetAddress, Boolean> ipMap = new HashMap<InetAddress, Boolean>();

	/** A map store Port->existence */
	public HashMap<Integer, Boolean> portMap = new HashMap<Integer, Boolean>();

	/** A map store InetAddress->existence */
	public HashMap<InetSocketAddress, Boolean> ipPortMap = new HashMap<InetSocketAddress, Boolean>();

	public ScanPort() {
		super();
	}

	public ScanPort(NetCard netCard) {
		super(netCard);
	}

	public int srcPort() {
		return srcPort;
	}

	public ScanPort srcPort(int value) {
		srcPort = value;
		return this;
	}

	/**
	 * Scan specified ports of specified Ip address
	 * 
	 * @param address The IP address to be scanned
	 * @param ports   An List of ports
	 */
	/** Scan specified ports on specified IP address */
	public ScanPort scan(List<InetAddress> addresses, List<Integer> ports) {
		// where network card starts
		netCard.onStart(e -> {
			// scan in a Thread
			NetUtil.runThread(() -> {
				if (!canceled) {
					doScan(addresses, ports);
				}

			});
		});

		// start the card
		canceled = false;
		netCard.filter("tcp and host " + srcAddress.getHostAddress());
		netCard.start();

		return this;
	}

	/** Scan specified ports of specified Ip address */
	public ScanPort scan(InetAddress address, int[] ports) {
		List<InetAddress> addresses = new ArrayList<InetAddress>();
		addresses.add(address);
		return scan(addresses, ArrayUtil.arrayToList(ports));
	}

	/**
	 * Scan specified ports of specified Ip address
	 * 
	 * @param address         The IP address to be scanned
	 * @param portDescription port description, such as "1, 80-90, common, fast"
	 */
	public ScanPort scan(InetAddress address, String portDescription) {
		List<InetAddress> addresses = new ArrayList<InetAddress>();
		addresses.add(address);
		return scan(addresses, NetUtil.strToPorts(portDescription));
	}

	/**
	 * Scan ip range on specified ports
	 * 
	 * @param address         The IP address to be scanned
	 * @param portDescription port description, such as "1, 80-90, common, fast"
	 */
	public ScanPort scan(String ipRange, String portDescription) {
		return scan(NetUtil.strToInetAddresses(ipRange), NetUtil.strToPorts(portDescription));
	}

	/** perform scan */
	protected ScanPort doScan(List<InetAddress> addresses, List<Integer> ports) {
		if (canceled)
			return this;

		long count = addresses.size() * ports.size();
		long total = (retryTimes + 1) * count; // total ip count
		long startTime = System.currentTimeMillis();
		int i = 0;

		// save addresses to ipMap
		if (retryCount == 0) {
			for (InetAddress address : addresses) {
				synchronized (ipMap) {
					ipMap.put(address, null);
				}
			}
		}

		// save ports to portMap
		if (retryCount == 0) {
			for (Integer port : ports) {
				synchronized (portMap) {
					portMap.put(port, null);
				}
			}
		}

		// iterate address-port
		for (InetAddress address : addresses) {
			for (int port : ports) {
				if (canceled)
					break;

				InetSocketAddress iAddr = new InetSocketAddress(address, port);
				// if already exist
				if (ipPortMap.getOrDefault(iAddr, null) != null) {
					continue;
				}

				// detect the ip-port
				detectPort(address, port);

				// report progress
				doProgress(i, count, startTime);

				i++;
			}
		}

		// increase retry count
		retryCount++;

		if (!canceled) {
			if (retryCount < retryTimes) {
				// if retry is in need, retry after a while
				NetUtil.setTimeout(() -> {
					doScan(addresses, ports);
				}, 800);

			} else {
				// there is no more retry, stop() after a while
				NetUtil.setTimeout(() -> {
					triggerOnProgress(total, total);
					netCard.stop();
				}, 800);
			}
		}

		return this;
	}

	/** detect specified ip and specified port */
	protected void detectPort(InetAddress addr, int port) {
		// create TCP SYN packet
		int src_port = hasOption(RANDOM_PORT) ? NetUtil.randomInt(1024, 65535) : srcPort();
		TcpPacket packet = TcpPacket.createSyn(srcAddress, src_port, addr, port, 1);

		// send packet
		netCard.sendPacket(packet);
	}

	/** Implement onPacket() of PacketListener */
	/** Implement onPacket() of PacketListener */
	@Override
	public void onPacket(Packet packet) {
		if (packet instanceof TcpPacket) {
			TcpPacket tcp = (TcpPacket) packet;
			if (tcp.syn() && tcp.ack()) {
				InetAddress src = tcp.srcIp();
				int port = tcp.srcPort();
				if (ipMap.containsKey(src) && portMap.containsKey(port)) {
					InetSocketAddress iAddr = new InetSocketAddress(src, port);
					ipPortMap.put(iAddr, true);
					triggerOnFind(src, port);
				}
			}
		}
	}

	/** Trigger onFind event */
	/** Trigger onFind event */
	protected void triggerOnFind(InetAddress addr, Object value) {
		// trigger onFind event
		for (NetEventListener l : findListeners) {
			l.onEvent(new NetEventArgs(this, addr, value));
		}
	}

	/** Return List of exist ports of specified ip address*/
	public List<Integer> getExistPort(InetAddress ip) {
		List<Integer> list = new ArrayList<Integer>();
		for (Map.Entry<InetSocketAddress, Boolean> entry : ipPortMap.entrySet()) {
			if (entry.getValue() != null) {
				InetSocketAddress iAddr = entry.getKey();
				if (ip.equals(iAddr.getAddress())) {
					list.add(iAddr.getPort());
				}
			}
		}

		return list;
	}

	/** Return exist InetSocketAddress (ip : port) */
	public List<InetSocketAddress> getIpPorts() {
		List<InetSocketAddress> list = new ArrayList<InetSocketAddress>();
		for (Map.Entry<InetSocketAddress, Boolean> entry : ipPortMap.entrySet()) {
			if (entry.getValue() != null) {
				InetSocketAddress iAddr = entry.getKey();
				list.add(iAddr);
			}
		}
		return list;
	}

}
