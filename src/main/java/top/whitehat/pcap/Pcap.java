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

import java.io.EOFException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import top.whitehat.pcap.PcapLib.BpfProgram;
import top.whitehat.pcap.PcapLib.PcapErrBuf;
import top.whitehat.pcap.PcapLib.PcapExtLibrary;
import top.whitehat.pcap.PcapLib.bpf_program;
import top.whitehat.pcap.PcapLib.pcap_pkthdr;
import top.whitehat.pcap.PcapLib.pcap_stat;
import top.whitehat.pcap.PcapLib.timeval;

/** Provides constants and utility methods for working with libpcap */
public final class Pcap implements AutoCloseable {

	/** Check whether libpcap is installed */
	public static void checkExists() {
		if (!Pcap.isExists()) {
			String prefix = Platform.isWindows() ? "Np" : "lib";
			String url = Platform.isWindows() ? "download https://npcap.com/dist/npcap-1.85.exe" : "";
//			String winpcap_url = "https://www.winpcap.org/install/bin/WinPcap_4_1_3.exe";
			throw new RuntimeException(prefix + "pcap not found, please install it, " + url);
		}
	}

	// error codes
	public final static int ERR_NOT_OPEN = 1;
	public final static int ERR_FILE_READ = 2;
	public final static int ERR_CAPTURE = 8;
	public final static int ERR_PACKET = 3;
	public final static int ERR_DUMP = 4;
	public final static int ERR_STAT = 5;
	public final static int ERR_OPEN_DUMP = 6;

	/** time unit: microseconds */
	public static final int MICROSECONDS = 0;

	/** time unit: nanoseconds */
	public static final int NANOSECONDS = 1;

	/** Bpf Compile Mode: OPTIMIZE */
	public final static int BPF_OPTIMIZE = 1;

	/** Bpf Compile Mode: NON_OPTIMIZE */
	public final static int BPF_NON_OPTIMIZE = 0;

	/** Direction of packets: Both inbound and outbound */
	public final static int DIRECTION_INOUT = 0;

	/** Direction of packets: inbound only */
	public final static int DIRECTION_IN = 1;

	/** Direction of packets: outbound only */
	public final static int DIRECTION_OUT = 2;

	/** Blocking mode: BLOCKING */
	public final static int BLOCKING = 0;

	/** Blocking mode: NON-BLOCKING */
	public final static int NONBLOCKING = 1;

	/** Swapped mode: NON-SWAPPED */
	public final static int NOT_SWAPPED = 0;

	/** Swapped mode: SWAPPED */
	public final static int SWAPPED = 1;

	/** Swapped mode: MAYBE_SWAPPED */
	public final static int MAYBE_SWAPPED = 2;

	/** promiscuous mode */
	public static final int PROMISCUOUS = 1;

	/** non-promiscuous mode */
	public static final int NON_PROMISCUOUS = 0;

	// Data Link Type
	// SEE: https://github.com/the-tcpdump-group/libpcap/blob/master/pcap/bpf.h

	/** Data Link Type: Null, no link layer header, the first byte is protocol */
	public static final int DLT_NULL = 0;

	/** Data Link Type: Ethernet (10Mb, 100Mb, 1000Mb, and up): 1 */
	public static final int DLT_ETHERNET = 1; // "Ethernet"
	public static final int DLT_EN10MB = 1; // Ethernet
	public static final int DLT_EN3MB = 2;

	/** Data Link Type: CHAOS : 5 */
	public static final int DLT_CHAOS = 5; //

	/** Data Link Type: IEEE802 "Token Ring" : 6 */
	public static final int DLT_TOKEN_RING = 6; //

	/** Data Link Type: SLIP: 8 */
	public static final int DLT_SLIP = 8;

	/** Data Link Type: Point-to-point Protocol: 9 */
	public static final int DLT_PPP = 9; // "PPP"

	/** Data Link Type: FDDI: 10 */
	public static final int DLT_FDDI = 10; // "FDDI"

	/** Data Link Type: RAW IP packet: 14 on OpenBSD, or 12 on the others. */
	public static final int DLT_RAW = 12;
	public static final int DLT_RAW_OPENBSD = 14;

	/** Data Link Type: PPP over Ethernet(PPPoE): 50 */
	public static final int DLT_PPP_ETHER = 50; // "PPP over serial with HDLC encapsulation"

	/** Data Link Type: IEEE 802.11 wireless: 105 */
	public static final int DLT_WIRELESS = 105; // "Wifi Wireless"

	/** Data Link Type: loopback: 108, such as from localhost */
	public static final int DLT_LOOP = 108; // "loopback"

	/** Data Link Type: Linux cooked-mode capture (SLL): 113 */
	public static final int DLT_LINUX_SLL = 113; // "Linux cooked-mode capture"

	/**
	 * Data Link Type: Radiotap: 127 - Header for 802.11 plus a number of bits of
	 * link-layer information including radio information, used by some recent BSD
	 * drivers as well as the madwifi Atheros driver for Linux.
	 */
	public static final int DLT_RADIO_TAP = 127; // "Radiotap"

	/** Data Link Type: Raw IPv4 Packet without link layer header */
	public static final int DLT_IPV4 = 228; // "Raw IPv4 Packet"

	/** Data Link Type: Raw IPv6 Packet without link layer header */
	public static final int DLT_IPV6 = 229; // "Raw IPv6 Packet"

	/** Data Link Type: DOCSIS MAC frames: 143 */
	public static final int DLT_DOCSIS = 143; // "DOCSIS"

	/**
	 * ​​BIOCSTIME​​ is an ioctl that configures how the packet buffer timestamps
	 * packets. The classic ​​BPF ioctl​​ used to set the kernel’s packet buffer’s
	 * time stamp resolution or behavior
	 */
	static final int SOLARIS_BIOCSTIME = 0x4201;

	/** whether library libpcap exists in current OS environment */
	public static boolean isExists() {
		return PcapLib.isExists();
	}

	/** return all network interface */
	public static List<PcapNetworkInterface> findAllDevs() {
		return PcapNetworkInterface.findAllDevs();
	}

	/**
	 * create Pcap object by name or ip, return null if not found.
	 * 
	 * @return
	 */
	public static Pcap of(String ipOrName) {
		PcapNetworkInterface nif = PcapNetworkInterface.find(ipOrName);
		return new Pcap(nif);
	}

	// ----------------------------------

	private PcapNetworkInterface _nif = null; // pcap network interface
	private String _deviceName = ""; // name of network interface
	private Pointer _handle = null; // handle pointer from pcap_open()
	private String _filter = ""; // BPF filter expression
	private int _direction = DIRECTION_INOUT; // controls whether we accept only incoming packets
	private int _snaplen = 2048; // snapshot length — the maximum number of bytes captured per packet.
	private int _promiscuousMode = PROMISCUOUS;
	private int _timeUnit = MICROSECONDS; // time unit, see: MICROSECONDS, NANOSECONDS
	private volatile int _dlt = 0; // data link type
	private ReentrantReadWriteLock _readWriteLock = new ReentrantReadWriteLock(true); // lock
	private static Object _compileLock = new Object(); // compile lock
	private static final Inet4Address NETMASK_ALL = PcapUtils.toIpv4("255.255.255.255");


	/** constructor */
	public Pcap(PcapNetworkInterface nif) {
		this._nif = nif;
		this._deviceName = this._nif.getName();
	}

	/** constructor */
	public Pcap(Pointer handle) {
		this._handle = handle;
		this._dlt = PcapLib.pcap_datalink(_handle);
	}

	/**
	 * Starts live packet capture for reading network traffic.<br>
	 * 
	 * @param timeoutMillis Read timeout. Most OSs buffer packets. The OSs pass the
	 *                      packets to Pcap4j after the buffer gets full or the read
	 *                      timeout expires. Must be non-negative. May be ignored by
	 *                      some OSs. 0 means disable buffering on Solaris. 0 means
	 *                      infinite on the other OSs. 1 through 9 means infinite on
	 *                      Solaris.
	 * 
	 * @return a new PcapHandle object.
	 * 
	 */
	public Pcap openLive(int timeoutMillis) {
		PcapErrBuf errbuf = new PcapErrBuf();
		_handle = PcapLib.pcap_open_live(_deviceName, _snaplen, _promiscuousMode, timeoutMillis, errbuf);

		if (_handle == null || errbuf.length() != 0) {
			throw new PcapException(ERR_CAPTURE, errbuf.toString());
		}

		if (timeoutMillis == 0 && Platform.isSolaris()) {
			timeval t = new timeval();
			t.tv_sec = new NativeLong(0);
			t.tv_usec = new NativeLong(0);

			int rc = PcapExtLibrary.INSTANCE.strioctl(PcapLib.getFdFromPcapT(_handle), SOLARIS_BIOCSTIME, t.size(),
					t.getPointer());

			if (rc < 0) {
				throw new PcapException(ERR_CAPTURE,
						"SBIOCSTIME: " + PcapLib.pcap_strerror(PcapLib.ERRNO_POINTER.getInt(0)).getString(0));
			}
		}

		this._dlt = PcapLib.pcap_datalink(_handle);		
		return this;
	}

	/**
	 * Open a file in the libpcap format to read packets.
	 * 
	 * 
	 * @param filename filename of the pcap file
	 * @return
	 */
	public static Pcap openOffline(String filename) {
		PcapErrBuf errbuf = new PcapErrBuf();
		Pointer handle = PcapLib.pcap_open_offline(filename, errbuf);
		if (handle == null || errbuf.length() != 0) {
			throw new PcapException(ERR_OPEN_DUMP, errbuf.toString());
		}

		Pcap p = new Pcap(handle);		
		return p;
	}

	public Pointer dumpOpen(String filename, boolean isAppend) {
		checkOpen();
		tryLock();

		Pointer prt = null;
		
		try {
			if (isAppend && PcapLib.getOptionFuncs() != null)
				PcapLib.getOptionFuncs().pcap_dump_open_append(_handle, filename);
			else
				prt = PcapLib.pcap_dump_open(_handle, filename);
			
			if (prt == null) {
				throw new PcapException(ERR_OPEN_DUMP, getError());
			}
		} finally {
			unlock();
		}
		
		return prt;
	}
	
	

	/** get the data link type */
	public int dataLinkType() {
		return _dlt;
	}

	/** set the data link type */
	public Pcap dataLinkType(int dlt) {
		tryLock();
		try {
			int rc = PcapLib.pcap_set_datalink(_handle, dlt);
			if (rc < 0) {
				throw new PcapException(rc, getError());
			}
		} finally {

		}
		this._dlt = dlt;
		return this;
	}

	/** Check whether this Pcap object is opened */
	public boolean isOpened() {
		return _handle != null;
	}

	/** Check whether this Pcap object is opened */
	protected void checkOpen() {
		if (!isOpened()) {
			throw new PcapException(ERR_NOT_OPEN, "not open");
		}
	}

	/** try lock handle */
	protected void tryLock() {
		checkOpen();
		if (!_readWriteLock.readLock().tryLock()) {
			throw new PcapException(ERR_NOT_OPEN, "not open");
		}
		checkOpen();
	}

	/** unlock handle */
	protected void unlock() {
		_readWriteLock.readLock().unlock();
	}

	/** set the Data Link Type of this PcapHandle */

	/** Return the filtering expression */

	/** get time unit */

	/** get time unit */
	public int timeUnit() {
		return _timeUnit;
	}

	/** set time unit */
	public Pcap timeUnit(int n) {
		_timeUnit = n;
		return this;
	}

	/** get direction */
	public int direction() {
		return this._direction;
	}

	/**
	 * Set direction flag, which controls whether we accept only incoming packets,
	 * only outgoing packets, or both. Note that, depending on the platform, some or
	 * all direction arguments might not be supported.
	 *
	 * @param direction direction to set.
	 */
	public Pcap direction(int d) {
		tryLock();
		try {
			_direction = d;
			int rc = PcapLib.pcap_setdirection(_handle, d);
			if (rc < 0) {
				throw new PcapException(rc, "Failed to set direction: " + getError());
			}
		} finally {
			unlock();
		}
		return this;
	}

	/**
	 * get the snapshot length — the maximum number of bytes captured per packet.
	 */
	public int snaplen() {
		return _snaplen;

	}

	/**
	 * set the snapshot length — the maximum number of bytes captured per packet.
	 */
	public Pcap snaplen(int n) {
		_snaplen = n;
		return this;
	}

	/**
	 * Indicates whether the ​​savefile​​ associated with the capture handle ​​ uses
	 * a ​​byte order​​ different from the host’s ​​native byte order​​.
	 */
	public boolean isSwapped() {
		tryLock();

		int rc;
		try {
			rc = PcapLib.pcap_is_swapped(_handle);
		} finally {
			unlock();
		}

		switch (rc) {
		case 0:
			return false; // SwappedType.NOT_SWAPPED;
		case 1:
			return true; // SwappedType.SWAPPED;
		case 2:
			return true; // SwappedType.MAYBE_SWAPPED;
		default:
			return true; // SwappedType.MAYBE_SWAPPED;
		}
	}

	/** Get the major version number of the pcap library */

	/** Get the major version number of the libpcap library */
	public int getMajorVersion() {
		tryLock();

		try {
			return PcapLib.pcap_major_version(_handle);
		} finally {
			unlock();
		}
	}

	/** Get the minor version number of the libpcap library */
	public int getMinorVersion() {
		tryLock();
		try {
			return PcapLib.pcap_minor_version(_handle);
		} finally {
			unlock();
		}
	}

	/** get filter */
	public String filter() {
		return _filter;
	}

	/** Set filter */
	public void filter(String bpfExpression) {
		_filter = bpfExpression;
		if (_handle != null)
			setFilter(_filter, BPF_OPTIMIZE, NETMASK_ALL);
	}

	/** set filter */
	protected void setFilter(String bpfExpression, int bpfCompileMode, Inet4Address netmask) {
		if (bpfExpression == null || netmask == null) {
			throw new NullPointerException();
		}

		tryLock();

		try {
			bpf_program prog = new bpf_program();
			try {
				int mask = PcapUtils.readInt(netmask.getAddress(), 0);
				int rc;
				synchronized (_compileLock) {
					rc = PcapLib.pcap_compile(_handle, prog, bpfExpression, bpfCompileMode, mask);
				}
				if (rc < 0) {
					throw new PcapException(rc, "Error occurred in pcap_compile: " + getError());
				}

				rc = PcapLib.pcap_setfilter(_handle, prog);
				if (rc < 0) {
					throw new PcapException(rc, "Error occurred in pcap_setfilter: " + getError());
				}

				this._filter = bpfExpression;
			} finally {
				PcapLib.pcap_freecode(prog);
			}
		} finally {
			unlock();
		}
	}

	/**
	 * compile filter expression.
	 *
	 * @param bpfExpression bpfExpression
	 * @param mode          mode
	 * @param netmask       netmask
	 * @return a {@link org.pcap4j.core.BpfProgram BpfProgram} object.
	 */
	protected BpfProgram compileFilter(String bpfExpression, int bpfCompileMode, Inet4Address netmask) {
		if (bpfExpression == null || netmask == null) {
			throw new NullPointerException();
		}

		tryLock();

		bpf_program prog;
		try {
			prog = new bpf_program();
			int rc;
			synchronized (_compileLock) {
				rc = PcapLib.pcap_compile(_handle, prog, bpfExpression, bpfCompileMode,
						PcapUtils.readInt(netmask.getAddress(), 0));
			}
			if (rc < 0) {
				throw new PcapException(rc, getError());
			}
		} finally {
			unlock();
		}

		return new BpfProgram(prog, bpfExpression);
	}

	/** set filter with compile mode */

	/** Set filter */
	protected void setFilter(String bpfExpression, int bpfCompileMode) {
		setFilter(bpfExpression, bpfCompileMode, NETMASK_ALL);
	}

	/** Set filter */

	/** set filter by BPF program */
	protected void setFilter(BpfProgram prog) {
		if (prog == null) {
			throw new NullPointerException("prog is null.");
		}
		tryLock();
		try {
			int rc = PcapLib.pcap_setfilter(_handle, prog.getProgram());
			if (rc < 0) {
				throw new PcapException(rc, "Failed to set filter: " + getError());
			}
		} finally {
			unlock();
		}

		this._filter = prog.getExpression();
	}

	/**
	 * Set blocking mode.<br>
	 * 
	 * Blocking mode​​ means that when reading from a live capture, if there is ​​no
	 * packet available right now​​, the read function will ​​wait/block ​until at
	 * least one packet arrives
	 */
	public Pcap blockingMode(boolean isBlocking) {
		tryLock();
		try {
			int mode = isBlocking ? BLOCKING : NONBLOCKING;
			PcapErrBuf errbuf = new PcapErrBuf();
			int rc = PcapLib.pcap_setnonblock(_handle, mode, errbuf);
			if (rc < 0) {
				throw new PcapException(rc, errbuf.toString());
			}
		} finally {
			unlock();
		}
		return this;
	}

	/**
	 * Get blocking mode.<br>
	 * 
	 * Blocking mode​​ means that when reading from a live capture, if there is ​​no
	 * packet available right now​​, the read function will ​​wait/block ​until at
	 * least one packet arrives
	 */
	public boolean blockingMode() {
		tryLock();
		PcapErrBuf errbuf = new PcapErrBuf();
		int rc;
		try {
			rc = PcapLib.pcap_getnonblock(_handle, errbuf);
		} finally {
			unlock();
		}

		if (rc == 0) {
			return true; // BLOCKING;
		} else if (rc > 0) {
			return false; // NONBLOCKING;
		} else {
			throw new PcapException(rc, errbuf.toString());
		}
	}

	/**
	 * get next packet
	 * 
	 * @return a {@link PcapPacket} object that contains captured packet. May be
	 *         null.
	 */
	public PcapPacket getNextPacket() {
		tryLock();

		pcap_pkthdr header = new pcap_pkthdr();
		header.setAutoSynch(false);
		Pointer packet;
		try {
			packet = PcapLib.pcap_next(_handle, header);
		} finally {
			unlock();
		}

		if (packet != null) {
			Pointer headerP = header.getPointer();
			byte[] data = packet.getByteArray(0, pcap_pkthdr.getCaplen(headerP));
			int len = pcap_pkthdr.getLen(headerP);
			return new PcapPacket(data, _dlt, buildTimestamp(headerP), len);
		} else {
			return null;
		}
	}

	/**
	 * get next packet(extended)
	 *
	 * @return a {@link PcapPacket} object that contains captured packet. Not null.
	 * @if an error occurs in the pcap native library.
	 * @throws EOFException     if packets are being read from a pcap file and there
	 *                          are no more packets to read from the file.
	 * @throws TimeoutException if packets are being read from a live capture and
	 *                          the timeout expired. @ if this PcapHandle is not
	 *                          open.
	 */
	public PcapPacket getNextPacketEx() throws EOFException, TimeoutException {
		tryLock();
		try {
			PointerByReference headerPP = new PointerByReference();
			PointerByReference dataPP = new PointerByReference();
			int rc = PcapLib.pcap_next_ex(_handle, headerPP, dataPP);
			switch (rc) {
			case 0:
				throw new TimeoutException();
			case 1:
				Pointer headerP = headerPP.getValue();
				Pointer dataP = dataPP.getValue();
				if (headerP == null || dataP == null) {
					throw new PcapException(ERR_PACKET,
							"Failed to get packet. *header: " + headerP + " *data: " + dataP);
				}

				return new PcapPacket(dataP.getByteArray(0, pcap_pkthdr.getCaplen(headerP)), _dlt,
						buildTimestamp(headerP), pcap_pkthdr.getLen(headerP));
			case -1:
				throw new PcapException(rc, "Error occurred in pcap_next_ex(): " + getError());
			case -2:
				throw new EOFException();
			default:
				throw new PcapException(rc, "Unexpected error occurred: " + getError());
			}
		} finally {
			unlock();
		}
	}

	/**
	 * packet loop capture.
	 * 
	 * A wrapper method for
	 * <code>int pcap_loop(pcap_t *, int, pcap_handler, u_char *)</code>. This
	 * method creates a Packet object from a captured packet using the packet
	 * factory and passes it to <code>listener.gotPacket(Packet)</code>. When a
	 * packet is captured, <code>listener.gotPacket(Packet)</code> is called in the
	 * thread which called the <code>loop()</code>. And then this PcapHandle waits
	 * for the thread to return from the <code>gotPacket()</code> before it
	 * retrieves the next packet from the pcap buffer.
	 *
	 * @param packetCount the number of packets to capture. -1 is equivalent to
	 *                    infinity. 0 may result in different behaviors between
	 *                    platforms and pcap library versions.
	 * @param listener    listener @ if an error occurs in the pcap native library.
	 * @throws InterruptedException if the loop terminated due to a call to
	 *                              {@link #breakLoop()}. @ if this PcapHandle is
	 *                              not open.
	 */
	public void loop(int packetCount, PcapListener listener) throws InterruptedException {
		loop(packetCount, listener, InnerExecutor.getInstance());
	}

	/**
	 * packet loop capture.
	 * 
	 * A wrapper method for
	 * <code>int pcap_loop(pcap_t *, int, pcap_handler, u_char *)</code>. This
	 * method creates a Packet object from a captured packet using the packet
	 * factory and passes it to <code>listener.gotPacket(Packet)</code>. When a
	 * packet is captured, the
	 * {@link java.util.concurrent.Executor#execute(Runnable) executor.execute()} is
	 * called with a Runnable object in the thread which called the
	 * <code>loop()</code>. Then, the Runnable object calls
	 * <code>listener.gotPacket(Packet)</code>. If
	 * <code>listener.gotPacket(Packet)</code> is expected to take a long time to
	 * process a packet, this method should be used with a proper executor instead
	 * of {@link #loop(int, PcapListener)} in order to prevent the pcap buffer from
	 * overflowing.
	 *
	 * @param packetCount the number of packets to capture. -1 is equivalent to
	 *                    infinity. 0 may result in different behaviors between
	 *                    platforms and pcap library versions.
	 * @param listener    listener
	 * @param executor    executor @ if an error occurs in the pcap native library.
	 * @throws InterruptedException if the loop terminated due to a call to
	 *                              {@link #breakLoop()}. @ if this PcapHandle is
	 *                              not open.
	 */
	public void loop(int packetCount, PcapListener listener, Executor executor) throws InterruptedException {
		if (listener == null || executor == null) {
			throw new NullPointerException();
		}

		tryLock();

		try {
			// starting loop
			int rc = PcapLib.pcap_loop(_handle, packetCount, new PcapHandler(listener, _dlt, executor), null);
			switch (rc) {
			case 0:
				// Finished loop
				break;
			case -1:
				throw new PcapException(rc, "Error occurred: " + getError());
			case -2:
				// broken
				throw new InterruptedException();
			default:
				throw new PcapException(rc, "Unexpected error occurred: " + getError());
			}
		} finally {
			unlock();
		}
	}

	/**
	 * Process packets ​by invoking the user’s ​​callback​​ for each packet.
	 *
	 * @param packetCount the maximum number of packets to process. If -1 is
	 *                    specified, all the packets in the pcap buffer or pcap file
	 *                    will be processed before returning. 0 may result in
	 *                    different behaviors between platforms and pcap library
	 *                    versions.
	 * @param listener    listener
	 * @return the number of captured packets. @ if an error occurs in the pcap
	 *         native library.
	 * @throws InterruptedException if the loop terminated due to a call to
	 *                              {@link #breakLoop()}. @ if this PcapHandle is
	 *                              not open.
	 */
	public int dispatch(int packetCount, PcapListener listener) throws InterruptedException {
		return dispatch(packetCount, listener, InnerExecutor.getInstance());
	}

	/**
	 * Process packets ​by invoking the user’s ​​callback​​ for each packet.
	 *
	 * @param packetCount the maximum number of packets to process. If -1 is
	 *                    specified, all the packets in the pcap buffer or pcap file
	 *                    will be processed before returning. 0 may result in
	 *                    different behaviors between platforms and pcap library
	 *                    versions.
	 * @param listener    listener
	 * @param executor    executor
	 * 
	 * @return the number of captured packets.
	 * 
	 */
	public int dispatch(int packetCount, PcapListener listener, Executor executor) throws InterruptedException {
		if (listener == null || executor == null) {
			throw new NullPointerException();
		}

		tryLock();

		int rc;
		try {
			rc = PcapLib.pcap_dispatch(_handle, packetCount, new PcapHandler(listener, _dlt, executor), null);
			if (rc < 0) {
				switch (rc) {
				case -1:
					throw new PcapException(rc, "Error occurred: " + getError());
				case -2:
					throw new InterruptedException();
				default:
					throw new PcapException(rc, "Unexpected error occurred: " + getError());
				}
			}
		} finally {
			unlock();
		}

		return rc;
	}

	/**
	 * packet loop dump.
	 *
	 * @param packetCount count of packet needed, -1 means loop forever.
	 * @param dumper      dumper
	 * @throws InterruptedException if the loop terminated due to a call to
	 *                              {@link #breakLoop()}.
	 */
	public void loop(int packetCount, PcapDumper dumper) throws InterruptedException {
		if (dumper == null) {
			throw new NullPointerException("dumper must not be null.");
		}

		tryLock();

		try {
			int rc = PcapLib.pcap_loop(_handle, packetCount, PcapLib.PCAP_DUMP, dumper.getDumper());
			switch (rc) {
			case 0:
				// finished dump loop
				break;
			case -1:
				throw new PcapException(rc, "Error occurred: " + getError());
			case -2:
				throw new InterruptedException();
			default:
				throw new PcapException(rc, "Unexpected error occurred: " + getError());
			}
		} finally {
			unlock();
		}
	}

	/**
	 * Breaks a loop which this handle is working on.
	 *
	 * The loop may not be broken immediately on some OSes because of buffering or
	 * something. As a workaround, letting this capture some bogus packets after
	 * calling this method may work.
	 * 
	 */
	public void breakLoop() {
		tryLock();
		try {
			PcapLib.pcap_breakloop(_handle);
		} finally {
			unlock();
		}
	}

	/**
	 * Returns a {@link Stream} instance representing a stream of captured packets.
	 * When this handle become unable to capture packets anymore (e.g. reaches EOF
	 * of the pcap file), the stream starts to supply nulls. This method locks this
	 * handle. Call {@link Stream#close()} to unlock.
	 *
	 * @return a stream of captured packets.
	 */
	public Stream<PcapPacket> stream() {
		tryLock();

		Stream<PcapPacket> stream = Stream.generate(() -> {
			PointerByReference headerPP = new PointerByReference();
			PointerByReference dataPP = new PointerByReference();

			while (true) {
				int rc = PcapLib.pcap_next_ex(_handle, headerPP, dataPP);
				switch (rc) {
				case 0:
					continue; // timeout
				case 1:
					Pointer headerP = headerPP.getValue();
					Pointer dataP = dataPP.getValue();
					if (headerP == null || dataP == null) {
						return null; // error
					}
					byte[] rawData = dataP.getByteArray(0, pcap_pkthdr.getCaplen(headerP));
					int len = pcap_pkthdr.getLen(headerP);
					return new PcapPacket(rawData, _dlt, buildTimestamp(headerP), len);
				case -1:
					return null; // error
				case -2:
					return null; // EOF
				default:
					return null;
				}
			}
		});

		stream.onClose(() -> _readWriteLock.readLock().unlock());
		return stream;
	}

	/**
	 * Send packet of bytes
	 * 
	 * @param bytes the data bytes to send
	 */
	public void sendPacket(byte[] bytes) {
		sendPacket(bytes, bytes.length);
	}

	/**
	 * Send packet of specified length of bytes
	 * 
	 * @param bytes the data bytes to send
	 * @param len   length
	 */
	public void sendPacket(byte[] bytes, int len) {
		if (bytes == null)
			throw new NullPointerException("bytes may not be null");

		tryLock();

		try {
			int rc = PcapLib.pcap_sendpacket(_handle, bytes, len);
			if (rc < 0) {
				throw new PcapException(rc, "Error occurred in pcap_sendpacket(): " + getError());
			}

		} finally {
			unlock();
		}
	}

	/** Return a PcapStat object */

	/** return statistics */
	public PcapStat getStats() {
		tryLock();

		try {
			if (Platform.isWindows()) {
				IntByReference pcapStatSize = new IntByReference();
				Pointer psp = PcapExtLibrary.INSTANCE.win_pcap_stats_ex(_handle, pcapStatSize);
				if (!getError().equals("Cannot retrieve the extended statistics from a file or a TurboCap port")) {
					if (pcapStatSize.getValue() != 24) {
						throw new PcapException(ERR_STAT, getError());
					}
					if (psp == null) {
						throw new PcapException(ERR_STAT, getError());
					}
					return new PcapStat(psp, true);
				}
			}

			pcap_stat ps = new pcap_stat();
			ps.setAutoSynch(false);
			int rc = PcapLib.pcap_stats(_handle, ps);
			if (rc < 0) {
				throw new PcapException(rc, getError());
			}

			return new PcapStat(ps.getPointer(), false);
		} finally {
			unlock();
		}
	}

	/** Return a list of DataLinkType */
	public List<Integer> listDatalinks() {
		tryLock();

		List<Integer> list;
		try {
			PointerByReference dltBufPP = new PointerByReference();
			int rc = PcapLib.pcap_list_datalinks(_handle, dltBufPP);
			if (rc < 0) {
				throw new PcapException(rc, getError());
			}

			Pointer dltBufP = dltBufPP.getValue();
			list = new ArrayList<Integer>(rc);
			for (int i : dltBufP.getIntArray(0, rc)) {
				list.add(i);
			}
			PcapLib.pcap_free_datalinks(dltBufP);
		} finally {
			unlock();
		}

		return list;
	}

	/** get error message */

	/** Return error message */
	public String getError() {
		tryLock();
		try {
			return PcapLib.pcap_geterr(_handle).getString(0);
		} finally {
			unlock();
		}
	}

	/** Closes the Pcap handle. */
	@Override
	public void close() {
		if (!isOpened())
			return;

		_readWriteLock.writeLock().lock();
		try {
			if (!isOpened())
				return; // Already closed
		} finally {
			_readWriteLock.writeLock().unlock();
		}

		PcapLib.pcap_close(_handle);
		_handle = null;		
	}

	@Override
	public String toString() {

		InetAddress addr = _nif.getFirstInetAddress(true);
		String ip = addr != null ? addr.getHostAddress() : "";

		StringBuilder sb = new StringBuilder(60);
		sb.append(getClass().getSimpleName()) //
				.append(" [Ip:").append(ip).append(" ]");
		return sb.toString();
	}

	/** Executor used by loop */
	private static final class InnerExecutor implements Executor {

		private InnerExecutor() {
		}

		private static final InnerExecutor INSTANCE = new InnerExecutor();

		public static InnerExecutor getInstance() {
			return INSTANCE;
		}

		@Override
		public void execute(Runnable command) {
			command.run();
		}

	}

	/** callback function when got packet */
	protected final class PcapHandler implements PcapLib.pcap_handler {

		private final int dlt;
		private final PcapListener listener;
		private final Executor executor;

		public PcapHandler(PcapListener listener, int dlt, Executor executor) {
			this.dlt = dlt;
			this.listener = listener;
			this.executor = executor;
		}

		@Override
		public void got_packet(Pointer args, Pointer header, final Pointer packet) {
			final Instant ts = buildTimestamp(header);
			final int len = pcap_pkthdr.getLen(header);
			final byte[] ba = packet.getByteArray(0, pcap_pkthdr.getCaplen(header));

			try {
				executor.execute(() -> listener.onPacket(new PcapPacket(ba, dlt, ts, len)));
			} catch (Throwable e) {
				// logger.error("The executor has thrown an exception.", e);
			}
		}

	}

	/** create timestamp from header */

	private Instant buildTimestamp(Pointer header) {
		long epochSecond = pcap_pkthdr.getTvSec(header).longValue();
		switch (_timeUnit) {
		case MICROSECONDS:
			return Instant.ofEpochSecond(epochSecond, pcap_pkthdr.getTvUsec(header).intValue() * 1000);
		case NANOSECONDS:
			return Instant.ofEpochSecond(epochSecond, pcap_pkthdr.getTvUsec(header).intValue());
		default:
			throw new AssertionError("Never get here.");
		}
	}

}
