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
package top.whitehat.net;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import top.whitehat.Dump;
import top.whitehat.LAN;
import top.whitehat.NetCard;
import top.whitehat.NetUtil;
import top.whitehat.packet.MacAddress;
import top.whitehat.packet.Packet;
import top.whitehat.packet.UdpPacket;


public class DatagramNetCard extends NetCard {

	/** selectable channel for UDP */
	protected DatagramChannel channel = null;

	/** A multiplexor of SelectableChannel objects. */
	protected Selector selector = null;

	/** ip address */
	private InetAddress ipAddress;

	/** Constructor */
	public DatagramNetCard() {
		super();
		this.displayName("java.net.DatagramSocket");
		setParams();
	}
	
	public DatagramNetCard(int port) {
		this();
		setPort(port);
	}

	/** Constructor: ipPort is a string like "192.168.100.1:100" */
	public DatagramNetCard(String ipPort) {
		super();
		this.displayName("java.net.DatagramSocket");

		if (ipPort != null) {
			try {
				String sIp = null;
				String sPort = null;
				int dot = ipPort.indexOf(":");

				if (NetUtil.isInteger(ipPort)) {
					sPort = ipPort;
				} else if (dot > 0 && ipPort.indexOf(".") > 0) {
					sIp = ipPort.substring(0, dot).trim();
					sPort = ipPort.substring(dot + 1).trim();
				} else {
					sIp = ipPort;
				}

				if (sIp != null && sIp.length() > 0)
					this.ipAddress = InetAddress.getByName(sIp);
				else
					this.ipAddress = InetAddress.getByName("0.0.0.0");

				if (sPort != null && sPort.length() > 0) {
					setPort(Integer.parseInt(sPort));
				}

			} catch (UnknownHostException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		setParams();
	}

	public Inet4Address getInet4Address() {
		return ipAddress instanceof Inet4Address ? (Inet4Address) ipAddress : null;
	}

	private void setParams() {
		if (ipAddress != null) {
			if (ipAddress.isAnyLocalAddress()) {
				MacAddress m = NetCard.getDefaultMac();
				this.mac(m);
			} else {
				this.mac(LAN.getMacOrDefault(ipAddress, MacAddress.BROADCAST));
			}
		}
	}

	public int getMTU() {
		return 2048;
	}

	public int getPacketBuffeSize() {
		return 2048;
	}

	public Packet sendPacket(Packet p) {
		UdpPacket udp = p.getPacket(UdpPacket.class);
		if (udp != null) {
			sendUdpPacket(udp);
			return udp;
		} else {
			throw new RuntimeException("DatagramNetCard cannot send a non-UDP packet");
		}
	}

	protected void sendUdpPacket(UdpPacket udp) {
		// create target
		InetSocketAddress target = new InetSocketAddress(udp.dstIp(), udp.dstPort());

		// put bytes into ByteBuffer
		byte[] buffer = udp.payload();
		ByteBuffer buf = ByteBuffer.allocate(this.getPacketBuffeSize());
		buf.put(buffer);
		buf.flip(); // change to read mode
		try {
			// send to target via channel
			channel.send(buf, target);
		} catch (IOException e) {
			// TODO: error
		}
		buf.clear();
	}

	@Override
	protected void doSendPacket(Packet p) {
		UdpPacket udp = p.getPacket(UdpPacket.class);
		if (udp != null) {
			sendUdpPacket(udp);
		} else {
			throw new RuntimeException("DatagramNetCard cannot send a non-UDP packet");
		}

	}

	public void createServer() throws IOException {
		// open channel
		channel = DatagramChannel.open();
		channel.configureBlocking(false);
		if (ipAddress != null) {
			channel.bind(new InetSocketAddress(ipAddress, getPort()));

		} else {
			channel.bind(new InetSocketAddress(getPort()));
		}

		// open selector
		selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ);
	}

	public void closeServer() {
		try {
			selector.close();
		} catch (IOException e) {
		}

		try {
			channel.close();
		} catch (IOException e) {
		}
	}

	@Override
	public void start() {
		try {
			createServer();
			triggerStart();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}

		this.setCanceled(false);

		// loop until canceled
		while (!isCanceled()) {
			try {
				// check for available data
				if (selector.isOpen() && selector.select() > 0) {
					Iterator<SelectionKey> it = selector.selectedKeys().iterator();
					// iterate SelectionKey
					while (it.hasNext() && !isCanceled()) {
						SelectionKey sk = it.next();
						if (sk.isReadable()) {
							// receive data
							ByteBuffer buf = ByteBuffer.allocate(getPacketBuffeSize());
							InetSocketAddress srcAddr = (InetSocketAddress) channel.receive(buf);
							buf.flip();

							InetAddress dstIp = ipAddress;
							int dstPort = getPort();
							if (channel.getLocalAddress() instanceof InetSocketAddress) {
								InetSocketAddress dstAddr = (InetSocketAddress) channel.getLocalAddress();
								if (NetUtil.ipVersion(srcAddr.getAddress()) == NetUtil
										.ipVersion(dstAddr.getAddress())) {
									dstIp = dstAddr.getAddress();
								}
								dstPort = dstAddr.getPort();
							}

							// convert to byte[]
							byte[] buffer = new byte[buf.limit()];
							System.arraycopy(buf.array(), 0, buffer, 0, buf.limit());

							// convert buffer to Packet
							Packet pkt = UdpPacket.create(srcAddr.getAddress(), srcAddr.getPort(), dstIp, dstPort,
									buffer);
							// call receive packet
							this.receivePacket(pkt);
						}
						it.remove();
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		closeServer();
	}


	@Override
	public void stop() {
		// TODO Auto-generated method stub
		setCanceled(true);
	}
	
	@Override
	public void readDump(String filename) {
		// TODO Auto-generated method stub

	}

	@Override
	public Dump openDump(String filename, boolean isAppend) {
		// TODO Auto-generated method stub
		return null;
	}

}
