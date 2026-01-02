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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import top.whitehat.net.NetCardFactory;
import top.whitehat.net.NetCardInfo;
import top.whitehat.packet.Ethernet;
import top.whitehat.packet.IHasIp;
import top.whitehat.packet.Layer2Packet;
import top.whitehat.packet.MacAddress;
import top.whitehat.packet.Packet;
import top.whitehat.packet.PacketProcessor;

/** A NetCard object can receive and send packets.​ */
public abstract class NetCard implements PacketProcessor {

	/** A map stores NetCard informations */
	private static List<NetCard> infos;

	/** Retrieve information of all network interface cards */
	public static List<NetCard> list() {
		if (infos == null) {
			infos = NetCardInfo.list();
		}
		return infos;
	}

	/** get default MAC Address */
	public static MacAddress getDefaultMac() {
		return NetCardInfo.getDefaultMac();
	}

	/**
	 * Open the network card of specified IP address or name.​
	 * 
	 * @param ipOrHostName IP address string or host Name
	 * @return return NetCard object if success, return null if not found.
	 */
	public static NetCard of(String ipOrHostName) {
		// if ip or name is empty
		if (ipOrHostName == null || ipOrHostName.length() == 0) {
			InetAddress addr = null;
			// if there is only one network card
			if (NetCard.list().size() == 1) {
				addr = NetCard.list().get(0).getInet4Address();
			} else {
				// find the local IP address that is connected to the Internet.
				addr = NetUtil.getInternetLocalAddress();
				if (addr == null)
					addr = NetCard.list().get(0).getInet4Address();
			}

			if (addr == null)
				throw new RuntimeException("cannot find network card");
			ipOrHostName = addr.getHostAddress();
		}

		return NetCardFactory.find(ipOrHostName);
	}

	/**
	 * Open the network card of specified IP address.​
	 * 
	 * @param address IP address
	 * @return return NetCard object if success, return null if not found.
	 */
	public static NetCard of(InetAddress address) {
		return of(address.getHostAddress());
	}

	/**
	 * Open the network card that is connected to the Internet.​
	 * 
	 * @return return NetCard object if success, return null if not found.
	 */
	public static NetCard inet() {
		// set ip to empty, means find the network card that is connected to the
		// Internet
		return of("");
	}

	/**
	 * ​​Start packet capture on the network card that has the specified IP address,
	 * using the specified packet filter.​
	 * 
	 * @param ipOrName IP address or host name. null means the network card
	 *                 connected to Internet
	 * @param filter   Packet filter string <a href=
	 *                 "https://www.kaitotek.com/resources/documentation/concepts/packet-filter/pcap-filter-syntax">SEE:
	 *                 pcap filter syntax</a>
	 * @param listener The handler that accepts the received packet.
	 */
	public static void capture(String ipOrName, String filter, PacketListener listener) {
		NetCard card = NetCard.of(ipOrName);
		card.filter(filter).onPacket(listener).start();
	}

	/**
	 * ​​Start packet capture on the network card that is connected to the Internet,
	 * using the specified packet filter.​
	 * 
	 * @param filter   Packet filter string <a href=
	 *                 "https://www.kaitotek.com/resources/documentation/concepts/packet-filter/pcap-filter-syntax">SEE:
	 *                 pcap filter syntax</a>
	 * @param listener The handler that accepts the received packet.
	 * 
	 * @return none
	 */
	public static void capture(String filter, PacketListener listener) {
		capture(null, filter, listener);
	}

	/**
	 * Start packet capture on the NetCard that is connected to the Internet.
	 * 
	 * @param listener The handler that accepts the received packet.
	 */
	public static void capture(PacketListener listener) {
		capture(null, null, listener);
	}

	// fields of an NetCard instance

	/** name of the network card */
	private String _name = "";

	/** description of the network card */
	private String _description = "";

	/** data link type */
	private int _dataLinkType = 0;

	/** MAC address */
	private MacAddress _mac = null;

	private boolean _dhcpEnabled = false;

	private InetAddress _dhcpServer = null;

	private List<NetCardAddress> netcardAddresses = new ArrayList<NetCardAddress>();

	/** indicate whether the capture is canceled */
	private boolean _canceled = false;

	/** onStart event listener */
	private List<NetEventListener> _startListenerList = new ArrayList<NetEventListener>();

	/** onStart event listener */
	private List<NetEventListener> _stopListenerList = new ArrayList<NetEventListener>();

	/** onPacket event listener */
	private List<PacketListener> _packetListenerList = new ArrayList<PacketListener>();

	/** filter string */
	private String _filter;

	/** timeout in milliseconds */
	private int _timeout = 10;
	
	/** cache for ip address */
	private Inet4Address ip4AddressCache = null;
	private Inet6Address ip6AddressCache = null;

	/** which port to listen to */
	private int port = 0;

	/** Constructor */
	protected NetCard() {
	}

	/** get the port that should listen to */
	public int getPort() {
		return port;
	}

	/** set the port that should listen to */
	public void setPort(int port) {
		this.port = port;
	}

	/** get List of NetCardAddress */
	public List<NetCardAddress> getNetcardAddresses() {
		return this.netcardAddresses;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[");
		sb.append("name='").append(displayName()).append("'");
		for (NetCardAddress nAddr : getNetCardAddresses()) {
			if (nAddr.getAddress() != null) {
				sb.append(", ip=").append(nAddr.getAddress().getHostAddress());
				if (nAddr.getGateway() != null)
					sb.append(", gateway=").append(nAddr.getGateway().getHostAddress());
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/** Check whether current capture action is canceled */
	public boolean isCanceled() {
		return _canceled;
	}

	/** Set canceled */
	public void setCanceled(boolean value) {
		_canceled = value;
	}

	/** Get the name */
	public String name() {
		return _name;
	}

	/** Set the name */
	public void name(String value) {
		_name = value;
	}

	/** Get display name */
	public String displayName() {
		return _description;
	}

	/** Set display name */
	public void displayName(String value) {
		_description = value;
	}

	/** get data link type */
	public int dataLinkType() {
		return this._dataLinkType;
	}

	/** set data link type */
	public void dataLinkType(int value) {
		this._dataLinkType = value;
	}

	/** get DHCP server */
	public InetAddress dhcpServer() {
		return this._dhcpServer;
	}

	/** set DHCP server */
	public void dhcpServer(InetAddress value) {
		this._dhcpServer = value;
	}

	/** get DHCP Enabled */
	public boolean dhcpEnabled() {
		return this._dhcpEnabled;
	}

	/** set DHCP Enabled */
	public void dhcpEnabled(boolean value) {
		this._dhcpEnabled = value;
	}

	/** Get MAC address of this network card */
	public MacAddress mac() {
		return _mac;
	}

	/** Set MAC address of this network card */
	public NetCard mac(MacAddress value) {
		_mac = value;
		return this;
	}

	/** Gets NetCardAddress list of this network card */
	public NetCardAddress getNetCardAddress(int index) {
		return netcardAddresses.get(index);
	}

	/** Gets NetCardAddress list of this network card */
	public List<NetCardAddress> getNetCardAddresses() {
		return netcardAddresses;
	}

	public boolean isInSameSubnet(InetAddress addr) {
		if (netcardAddresses.size() == 0)
			return false;

		for (NetCardAddress nAddr : netcardAddresses) {
			if (nAddr.isInSameSubnet(addr)) {
				return true;
			}
		}
		return false;
	}

	/** Put a gateway address into NetCardAddress list */
	public boolean putGateWay(InetAddress gateway) {
		for (NetCardAddress nAddr : netcardAddresses) {
			if (NetUtil.isInSameSubnet(gateway, nAddr.getAddress(), nAddr.getNetworkPrefixLength())) {
				nAddr.setGateway(gateway);
				return true;
			}
		}
		return false;
	}

	/**
	 * Alias of getInet4Address(). Gets the Inet4Address of the network card, or
	 * returns null if it fails.​
	 */
	public Inet4Address ip() {
		return getInet4Address();
	}

	/**
	 * Returns the network prefix length for the Inet4Address of the network card.​
	 */
	public int getNetworkPrefixLength() {
		List<NetCardAddress> addrs = this.getNetCardAddresses();
		for (NetCardAddress addr : addrs) {
			if (addr.getAddress() instanceof Inet4Address) {
				return addr.getNetworkPrefixLength();
			}
		}
		return 32;
	}

	/**
	 * Return IP range of the whole LAN of this NetCard, Which may looks like:
	 * "192.168.0.1/24"
	 */
	public String subnet() {
		return ip().getHostAddress() + "/" + getNetworkPrefixLength();
	}

	

	/**
	 * Gets the Inet4Address of the network card, or returns null if it fails.​
	 */
	public Inet4Address getInet4Address() {
		if (ip4AddressCache == null) {
			List<NetCardAddress> addrs = this.getNetCardAddresses();
			for (NetCardAddress addr : addrs) {
				if (addr.getAddress() instanceof Inet4Address) {
					ip4AddressCache = (Inet4Address) addr.getAddress();
				}
			}
		}
		return ip4AddressCache;
	}

	/**
	 * Gets the Inet6Address of the network card, or returns null if it fails.​
	 */
	public Inet6Address getInet6Address() {
		if (ip6AddressCache == null) {
			List<NetCardAddress> addrs = this.getNetCardAddresses();
			for (NetCardAddress addr : addrs) {
				if (addr.getAddress() instanceof Inet6Address) {
					ip6AddressCache = (Inet6Address) addr.getAddress();
				}
			}
		}
		return ip6AddressCache;
	}

	/** get filter string */
	public String filter() {
		return _filter;
	}

	/** set filter string */
	public NetCard filter(String filterStr) {
		_filter = filterStr;
		return this;
	}

	/** get timeout (in milliseconds) */
	public int timeout() {
		return _timeout;
	}

	/** set timeout (in milliseconds) */
	public NetCard timeout(int value) {
		_timeout = value;
		return this;
	}

	/** Trigger onStart event */
	protected void triggerStart() {
		for (NetEventListener listener : _startListenerList)
			listener.onEvent(new NetEventArgs(this));
	}

	/** Trigger onStop event */
	protected void triggerStop() {
		for (NetEventListener listener : _stopListenerList)
			listener.onEvent(new NetEventArgs(this));
	}

	/**
	 * Define an 'onStart' event listener that is triggered when packet capture
	 * starts.​
	 */
	public NetCard onStart(NetEventListener listener) {
		_startListenerList.add(listener);
		return this;
	}

	/**
	 * Define an 'onStop' event listener that is triggered when packet capture
	 * ends.​
	 */
	public NetCard onStop(NetEventListener listener) {
		_stopListenerList.add(listener);
		return this;
	}

	/**
	 * Define an 'onPacket' event listener that is triggered when a packet arrives.​
	 */
	public NetCard onPacket(PacketListener listener) {
		_packetListenerList.add(listener);
		return this;
	}

	/**
	 * This method is called when a packet is sent. The child class should implement
	 * this method.​
	 */
	protected abstract void doSendPacket(Packet p);

	/** receive packet */
	@Override
	public void receivePacket(Packet p) {
		if (_packetListenerList != null) {
			// set processor of the packet to this object
			p.processor(this);

			// trigger listener
			for (PacketListener listener : _packetListenerList)
				listener.onPacket(p);
		}
	}

	public MacAddress getSendingMac(InetAddress address) {
		if (address == null)
			return MacAddress.BROADCAST;

		if (netcardAddresses.size() == 0)
			return MacAddress.BROADCAST;

		NetCardAddress nAddr = netcardAddresses.get(0);
		if (nAddr.isInSameSubnet(address) || address.isMulticastAddress()) {
			return LAN.getMacOrDefault(address, MacAddress.BROADCAST);
		} else {
			return LAN.getMacOrDefault(nAddr.getGateway(), MacAddress.BROADCAST);
		}
	}

	/** Warps the specified packet, and create a layer2 packet */
	public Packet createLayer2Packet(Packet p) {
		MacAddress srcMac = mac();
		InetAddress addr = p instanceof IHasIp ? ((IHasIp) p).dstIp() : null;
		MacAddress dstMac = getSendingMac(addr);
		return Ethernet.create(p, srcMac, dstMac);
	}

	/** send packet, return the packet sent */
	@Override
	public Packet sendPacket(Packet p) {
		Packet layer2 = (p.root() instanceof Layer2Packet) ? p : createLayer2Packet(p);
		doSendPacket(layer2);
		return layer2;
	}

	/**
	 * This method is called when starting packet capture. <br>
	 * The child class should implement this method.​
	 */
	public abstract void start();

	/**
	 * This method is called when stopping packet capture. <br>
	 * The child class should implement this method
	 */
	public abstract void stop();

	/**
	 * open a dumper file
	 * 
	 * @param filename the filename to save dumped data
	 * 
	 * @param isAppend Indicate whether append data to existing file
	 * 
	 * @return Dump object
	 */
	public abstract Dump openDump(String filename, boolean isAppend);

	/**
	 * open a dumper file
	 * 
	 * @param filename the filename to save dumped data.<br>
	 *                 if the file exists, the file is overwritten.
	 * 
	 * @return Dump object
	 */
	public Dump openDump(String filename) {
		return openDump(filename, false);
	}

	/**
	 * Read dump file. For each packet in the file, onPacket() is triggered
	 */
	public abstract void readDump(String filename);

}
