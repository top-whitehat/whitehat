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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetPermission;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import top.whitehat.pcap.PcapLib.PcapErrBuf;
import top.whitehat.pcap.PcapLib.pcap_addr;
import top.whitehat.pcap.PcapLib.pcap_if;
import top.whitehat.pcap.PcapLib.sockaddr_dl;
import top.whitehat.pcap.PcapLib.sockaddr_ll;

/** NetworkInterface for libpcap */
public class PcapNetworkInterface {

	/** cache of network interface */
	private static List<PcapNetworkInterface> nifs = null;

	/** find all network interfaces */
	private static List<PcapNetworkInterface> findAllDevs0() {
		PointerByReference alldevsPR = new PointerByReference();
		PcapErrBuf errbuf = new PcapErrBuf();

		int rc = PcapLib.pcap_findalldevs(alldevsPR, errbuf);
		if (rc != 0)
			throw new PcapException(rc, "Error(" + rc + ") " + errbuf);

		Pointer alldevsp = alldevsPR.getValue();
		if (alldevsp == null)
			return Collections.emptyList();

		pcap_if pcapIf = new pcap_if(alldevsp);
		List<PcapNetworkInterface> ifList = new ArrayList<PcapNetworkInterface>();

		try {
			for (pcap_if pif = pcapIf; pif != null; pif = pif.next) {
				ifList.add(new PcapNetworkInterface(pif, true));
			}
			return ifList;
		} finally {
			PcapLib.pcap_freealldevs(pcapIf.getPointer());
		}
	}

	/** return all network interface */
	public static List<PcapNetworkInterface> findAllDevs() {
		if (nifs == null || nifs.size() == 0)
			nifs = findAllDevs0();
		return nifs;
	}

	/** get network interfaces of specified index */
	public static PcapNetworkInterface getByIndex(int index) {
		if (index >= 0 && index < findAllDevs().size())
			return findAllDevs().get(index).setIndex(index);
		return null;
	}

	/**
	 * Searches for the network interface with the specified name.
	 *
	 * @param name The name of the network interface.
	 *
	 * @return A {@code NetworkInterface} with the specified name, or {@code null}
	 *         if there is no network interface with the specified name.
	 *
	 * @throws SocketException      If an I/O error occurs.
	 *
	 * @throws NullPointerException If the specified name is {@code null}.
	 */
	public static PcapNetworkInterface getByName(String name) {
		return find(name);
	}

	/**
	 * Convenience method to search for a network interface that has the specified
	 * Internet Protocol (IP) address bound to it.
	 * <p>
	 * If the specified IP address is bound to multiple network interfaces it is not
	 * defined which network interface is returned.
	 *
	 * @param addr The {@code InetAddress} to search with.
	 *
	 * @return A {@code NetworkInterface} or {@code null} if there is no network
	 *         interface with the specified IP address.
	 *
	 * @throws SocketException      If an I/O error occurs.
	 *
	 * @throws NullPointerException If the specified address is {@code null}.
	 */
	public static PcapNetworkInterface getByInetAddress(InetAddress addr) {
		int index = findIndex(null, addr);
		return getByIndex(index);
	}

	/**
	 * find index of the network interface by name or address, return -1 if not
	 * found.
	 */
	public static int findIndex(String ipOrName, InetAddress addr) {
		try {
			findAllDevs();

			// find each device
			for (int index = 0; index < nifs.size(); index++) {
				PcapNetworkInterface nif = nifs.get(index);

				if (ipOrName != null) {
					// match by name
					String devname = nif.getName();
					if (devname != null && devname.contains(ipOrName)) {
						return index;
					}

					// match by display name
					String desc = nif.getDisplayName();
					if (desc != null && desc.contains(ipOrName))
						return index;
				}

				// find by ip address
				List<PcapAddress> addresses = nif.getAddresses();
				for (PcapAddress pcapAddress : addresses) {
					// match by addr
					if (addr != null && addr.equals(pcapAddress.getAddress()))
						return index;

					if (ipOrName != null) {
						String ip = pcapAddress.getAddress().getHostAddress();
						if (ip != null && ip.contains(ipOrName)) {
							return index;
						}
					}
				}
			}
		} catch (Exception e) {

		}
		return -1;
	}

	/** find index of the network interface by name, return -1 if not found. */
	public static int findIndex(String ipOrName) {
		return findIndex(ipOrName, null);
	}

	/** find network interface by name or ip, return null if not found. */
	public static PcapNetworkInterface find(String ipOrName) {
		int index = findIndex(ipOrName);
		return getByIndex(index);
	}

	/**
	 * Unspecified address family. This value is defined in
	 * <code>&lt;sys/socket.h&gt;</code> as 0.
	 */
	public static final short AF_UNSPECIFIED = 0;

	/**
	 * Address family for IPv4. This value needs to be the same as AF_INET defined
	 * in <code>&lt;sys/socket.h&gt;</code>. This value may vary depending on OS.
	 * This value is set to 2 by default and can be changed by setting the property
	 * <code>org.pcap4j.af.inet</code> (system property or
	 * pcap4j-core.jar/org/pcap4j/pcap4j.properties).
	 *
	 * @see org.pcap4j.Pcap4jPropertiesLoader
	 */
	public static final short AF_INET = 2;

	/**
	 * Address family for IPv6. This value needs to be the same as AF_INET6 defined
	 * in <code>&lt;sys/socket.h&gt;</code>. This value varies depending on OS. By
	 * default, this value is set to 30 on Mac OS X, 28 on FreeBSD, 10 on Linux, and
	 * 23 on the others. This value can be changed by setting the property
	 * <code>org.pcap4j.af.inet6</code> (system property or
	 * pcap4j-core.jar/org/pcap4j/pcap4j.properties).
	 *
	 * @see org.pcap4j.Pcap4jPropertiesLoader
	 */
	public static final short AF_INET6 = 10;

	/**
	 * Address family for low level packet interface. This value needs to be the
	 * same as AF_PACKET defined in <code>&lt;sys/socket.h&gt;</code>. This value
	 * may vary depending on OS. This value is set to 17 by default and can be
	 * changed by setting the property <code>org.pcap4j.af.packet</code> (system
	 * property or pcap4j-core.jar/org/pcap4j/pcap4j.properties).
	 *
	 * @see org.pcap4j.Pcap4jPropertiesLoader
	 */
	public static final short AF_PACKET = 17;

	/**
	 * Address family for link layer interface. This value needs to be the same as
	 * AF_LINK defined in <code>&lt;sys/socket.h&gt;</code>. This value may vary
	 * depending on OS. This value is set to 18 by default and can be changed by
	 * setting the property <code>org.pcap4j.af.link</code> (system property or
	 * pcap4j-core.jar/org/pcap4j/pcap4j.properties).
	 *
	 * @see org.pcap4j.Pcap4jPropertiesLoader
	 */
	public static final short AF_LINK = 18;

	// private constants

	private static final int PCAP_IF_LOOPBACK = 0x00000001;
	private static final int PCAP_IF_UP = 0x00000002;
	private static final int PCAP_IF_RUNNING = 0x00000004;

	// private fields

	private int index;
	private String name;
	private String description;
	private List<PcapAddress> addresses = new ArrayList<PcapAddress>();
	private List<byte[]> hardwareAddresses = new ArrayList<byte[]>();
	private boolean loopBack;
	private boolean up;
	private boolean running;
	private boolean local;

	/** constructor */
	public PcapNetworkInterface(pcap_if pif, boolean local) {
		this.name = pif.name;
		this.description = pif.description;

		for (pcap_addr pcapAddr = pif.addresses; pcapAddr != null; pcapAddr = pcapAddr.next) {
			// skip empty address
			if (isEmpty(pcapAddr))
				continue;

			short sa_family = pcapAddr.addr != null ? pcapAddr.addr.getSaFamily()
					: pcapAddr.netmask != null ? pcapAddr.netmask.getSaFamily()
							: pcapAddr.broadaddr != null ? pcapAddr.broadaddr.getSaFamily()
									: pcapAddr.dstaddr != null ? pcapAddr.dstaddr.getSaFamily() : AF_UNSPECIFIED;

			if (sa_family == AF_INET) {
				addresses.add(new PcapIpV4Address(pcapAddr, sa_family));

			} else if (sa_family == AF_INET6) {
				addresses.add(new PcapIpV6Address(pcapAddr, sa_family));

			} else {
				if (Platform.isLinux() && sa_family == AF_PACKET) {
					sockaddr_ll sll = new sockaddr_ll(pcapAddr.addr.getPointer());
					byte[] addr = sll.sll_addr;
					int addrLength = sll.sll_halen & 0xFF;
					if (addrLength == 6) {
						hardwareAddresses.add(PcapUtils.readBytes(addr, 0, 6));
					} else if (addr.length == 0) {
						continue;
					} else if (addr.length != addrLength) {
						continue;  // skip addr that the length is mismatch
					} else {
						hardwareAddresses.add(PcapUtils.readBytes(addr, 0, addrLength));
					}
				} else if ((Platform.isMac() || Platform.isFreeBSD() || Platform.isOpenBSD())
						|| Platform.iskFreeBSD() && sa_family == AF_LINK) {
					sockaddr_dl sdl = new sockaddr_dl(pcapAddr.addr.getPointer());
					byte[] addr = sdl.getAddress();
					if (addr.length == 6) {
						hardwareAddresses.add(PcapUtils.copy(addr));
					} else if (addr.length == 0) {
						continue;
					} else {
						hardwareAddresses.add(PcapUtils.copy(addr));
					}
				} else {
					// ignore unknown address family
				}
			}
		}

		// In Windows, MAC address will be extracted here
		if (Platform.isWindows()) {
			byte[] mac = PacketDll.getMacAddress(name);
			if (mac != null) hardwareAddresses.add(mac);
		}

		this.loopBack = (pif.flags & PCAP_IF_LOOPBACK) != 0;
		this.up = (pif.flags & PCAP_IF_UP) != 0;
		this.running = (pif.flags & PCAP_IF_RUNNING) != 0;
		this.local = local;
	}

	

	/** whether pcapAddr is empty */
	private static boolean isEmpty(pcap_addr pcapAddr) {
		return pcapAddr.addr == null && pcapAddr.netmask == null && pcapAddr.broadaddr == null
				&& pcapAddr.dstaddr == null;
	}

	/**
	 * Get the name of this network interface.
	 *
	 * @return the name of this network interface
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the display name of this network interface. A display name is a human
	 * readable String describing the network device.
	 *
	 * @return a non-empty string representing the display name of this network
	 *         interface, or null if no display name is available.
	 */
	public String getDisplayName() {
		return description;
	}

	/**
	 * Returns the Maximum Transmission Unit (MTU) of this interface.
	 *
	 * @return the value of the MTU for that interface.
	 */
	public int getMTU() {
		try {
			NetworkInterface nif = getNetworkInterface();
			return nif == null ? 0 : nif.getMTU();
		} catch (SocketException e) {
		}
		return 0;
	}

	/** Return NetworkInterface object of this device */
	public NetworkInterface getNetworkInterface() {
		try {
			if (addresses.size() > 0) {
				InetAddress addr = addresses.get(0).getAddress();
				return NetworkInterface.getByInetAddress(addr);
			}
		} catch (SocketException e) {

		}
		return null;
	}

	/**
	 * Return first InetAddress of this network interface
	 * 
	 * @param isIpV4 whether need Ipv4
	 * 
	 * @return return InetAddress, return null if failed.
	 */
	public InetAddress getFirstInetAddress(boolean isIpV4) {
		for (int i = 0; i < addresses.size(); i++) {
			InetAddress addr = addresses.get(i).getAddress();
			if (isIpV4 && addr instanceof Inet4Address)
				return addr;

			if (!isIpV4 && addr instanceof Inet6Address)
				return addr;
		}
		return null;
	}

	/**
	 * Get an Enumeration with all or a subset of the InetAddresses bound to this
	 * network interface.
	 * <p>
	 * If there is a security manager, its {@code checkConnect} method is called for
	 * each InetAddress. Only InetAddresses where the {@code checkConnect} doesn't
	 * throw a SecurityException will be returned in the Enumeration. However, if
	 * the caller has the {@link NetPermission}("getNetworkInformation") permission,
	 * then all InetAddresses are returned.
	 *
	 * @return an Enumeration object with all or a subset of the InetAddresses bound
	 *         to this network interface
	 * @see #inetAddresses()
	 */
	public Enumeration<InetAddress> getInetAddresses() {
		return enumerationFromList(addresses);
	}

	/**
	 * Returns the hardware address (usually MAC) of the interface
	 *
	 * @return a byte array containing the address, or {@code null} if the address
	 *         doesn't exist. *
	 */
	public byte[] getHardwareAddress() {
		if (hardwareAddresses.size() > 0)
			return hardwareAddresses.get(0);
		return null;
	}

	/**
	 * Returns the index of this network interface. The index is an integer greater
	 * or equal to zero, or {@code -1} for unknown. This is a system specific value
	 * and interfaces with the same name can have different indexes on different
	 * machines.
	 *
	 * @return the index of this network interface or {@code -1} if the index is
	 *         unknown
	 */
	public int getIndex() {
		return index;
	}

	/** set the index of this network interface. */
	PcapNetworkInterface setIndex(int index) {
		this.index = index;
		return this;
	}

	/** create enumeration from List */
	private static Enumeration<InetAddress> enumerationFromList(List<PcapAddress> a) {
		return new Enumeration<InetAddress>() {
			int i = 0;

			@Override
			public InetAddress nextElement() {
				if (i < a.size()) {
					return a.get(i++).getAddress();
				} else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public boolean hasMoreElements() {
				return i < a.size();
			}
		};
	}

	public List<PcapAddress> getAddresses() {
		return addresses;
	}

	/**
	 * Returns whether a network interface is a loopback interface.
	 *
	 * @return {@code true} if the interface is a loopback interface.
	 */
	public boolean isLoopBack() {
		return loopBack;
	}

	/**
	 * Returns whether a network interface is up and running.
	 *
	 * @return {@code true} if the interface is up and running.
	 */
	public boolean isUp() {
		return up;
	}

	/**
	 * Returns if this network interface is running. This method may always return
	 * false on some environments.
	 *
	 * @return true if the network interface represented by this object is running;
	 *         false otherwise.
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Returns true if the network interface represented by this object is a local
	 * interface; false otherwise.
	 */
	public boolean isLocal() {
		return local;
	}

	@Override
	public String toString() {
		String ip = addresses.size() == 0 ? "" : addresses.get(0).getAddress().getHostAddress();
		return getClass().getSimpleName() + "{" + "display name='" + description + '\'' + ", ip='" + ip + '\'' + '}';
	}

}
