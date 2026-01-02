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

import static top.whitehat.pcap.Pcap.*;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Pointer;

import top.whitehat.LAN;
import top.whitehat.NetCard;
import top.whitehat.NetCardAddress;
import top.whitehat.packet.Ethernet;
import top.whitehat.packet.IpPacket;
import top.whitehat.packet.MacAddress;
import top.whitehat.packet.Packet;
import top.whitehat.packet.PppPacket;
import top.whitehat.pcap.Pcap;
import top.whitehat.pcap.PcapAddress;
import top.whitehat.pcap.PcapDumper;
import top.whitehat.pcap.PcapException;
import top.whitehat.pcap.PcapListener;
import top.whitehat.pcap.PcapNetworkInterface;
import top.whitehat.pcap.PcapPacket;

public class PcapNetCard extends NetCard {

	@FunctionalInterface
	public static interface PcapEventHandler {
		public void onEvent(PcapNetCard card, Object value);
	}
	
	public static PcapNetCard of(PcapNetworkInterface nif) {
		PcapNetCard ret = new PcapNetCard();
		ret.setInterface(nif);
		return ret;
	}

	/** Get a list of NetCard of this machine */
	public static List<NetCard> getList() {
		List<PcapNetworkInterface> nifs = PcapNetworkInterface.findAllDevs();
		List<NetCard> ret = new ArrayList<NetCard>();
		for (PcapNetworkInterface nif : nifs) {
			ret.add(PcapNetCard.of(nif));
		}
		return ret;
	}

	/** Wrap a PcapPacket object, convert it to a Packet object */
	public Packet wrap(PcapPacket pkt) {
		int dlt = pkt.getDatalinkType();

		switch (dlt) {
		case DLT_ETHERNET:
			return new Ethernet(pkt.getRawData()).child();
		case DLT_PPP:
			return new PppPacket(pkt.getRawData()).child();
		case DLT_IPV4:
		case DLT_IPV6:
		case DLT_RAW:
		case DLT_RAW_OPENBSD:
			return IpPacket.wrap(pkt.getRawData()).child();
		default:
			// TODO: other data link type
			throw new RuntimeException("data link type " + dlt + " not supported");
		}
	}

	/** the network interface */
	private PcapNetworkInterface nif;

	/** Pcap object */
	private Pcap pcap;
	
	private List<PcapEventHandler> onStartHandlers = new ArrayList<>();

	private List<PcapEventHandler> onStopHandlers = new ArrayList<>();


	public PcapNetCard() {
		super();
	}

	public PcapNetCard setInterface(PcapNetworkInterface nif) {
		this.nif = nif;

		// create Pcap object
		pcap = new Pcap(nif);

		// init MAC address
		byte[] bs = nif.getHardwareAddress();
		if (bs != null && bs.length == 6) {
			this.mac(new MacAddress(bs));
		}

		// init card address
		List<PcapAddress> addrs = nif.getAddresses();
		for (PcapAddress addr : addrs) {
			NetCardAddress a = new NetCardAddress();
			a.setAddress(addr.getAddress());
			a.setNetworkPrefixLength(addr.getNetworkPrefixLength());
			a.setGateway(LAN.findGateway(addr.getAddress()));
			this.getNetCardAddresses().add(a);
		}

		return this;
	}

	/** constructor */
	public PcapNetCard find(String ipOrName) {
		Pcap.checkExists();

		// find network interface
		nif = PcapNetworkInterface.find(ipOrName);
		if (nif == null)
			throw new RuntimeException("cannot find " + ipOrName);
		return setInterface(nif);

	}

	
	public void onStart(PcapEventHandler handler) {
		this.onStartHandlers.add(handler);
	}
	
	public void onStop(PcapEventHandler handler) {
		this.onStopHandlers.add(handler);
	}

	protected void triggerEventHandlers(List<PcapEventHandler> handlers, Object value) {
		for (PcapEventHandler handler : handlers) {
			handler.onEvent(this, value);
		}
	}

	protected void triggerOnStart() {
		triggerEventHandlers(onStartHandlers, null);
	}

	protected void triggerOnStop() {
		triggerEventHandlers(onStopHandlers, null);
	}
	
	@Override
	public String displayName() {
		return nif == null ? null : nif.getDisplayName();
	}

	@Override
	public NetCard filter(String filterStr) {
		super.filter(filterStr);

		// set filter
		if (filterStr != null && filterStr.length() > 0 && pcap != null) {
			try {
				pcap.filter(filterStr);
			} catch (PcapException e) {
				throw new RuntimeException("cannot set filter " + e.getMessage());
			}
		}
		return this;
	}

	/** start listen to the port */
	protected void startListen() {
		if (getPort() == 0)
			return;
	}

	/** top listen to the port */
	protected void stopListen() {
		if (getPort() == 0)
			return;
	}

	@Override
	public void start() {
		if (pcap.isOpened()) {
			pcap.close();
		}

		setCanceled(false);

		if (!pcap.isOpened()) {
			pcap.openLive(this.timeout());
			triggerOnStart();
		}

		// set filter
		filter(filter());

		// start listen to the port
		startListen();

		int errorCount = 0;
		while (errorCount < 3) {
			try {
				// create listener
				PcapPacketListener l = new PcapPacketListener(this);

				triggerStart();

				// loop listen
				pcap.loop(-1, l);
			} catch (PcapException e) {
				errorCount++;
				throw new RuntimeException("error capture : " + e.getMessage());
			} catch (InterruptedException e) {
				if (isCanceled()) {
					break;
				} else {
					errorCount++;
					throw new RuntimeException("capture interrupted : " + e.getMessage());
				}
			}
		}

		triggerStop();
	}

	@Override
	public void stop() {
		setCanceled(true);

		stopListen();

		try {
			pcap.breakLoop();
		} catch (Exception e) {

		}
		
		triggerOnStop();
		// pcap.close(); //TODO: why ???
		
	}

	@Override
	protected void doSendPacket(Packet p) {
		pcap.sendPacket(p.array());
	}

	@Override
	public void readDump(String filename) {
		// open offline file (dump file)
		pcap = Pcap.openOffline(filename);
		triggerOnStart();

		setCanceled(false);

		filter(filter());

		// create listener
		PcapPacketListener l = new PcapPacketListener(this);

		// trigger onStart
		triggerStart();

		// loop listen
		try {
			pcap.loop(-1, l);
			this.stop();
		} catch (InterruptedException e) {
		}

	}
	
	// Implement PcapListener
	public class PcapPacketListener implements PcapListener {
		PcapNetCard card;

		public PcapPacketListener(PcapNetCard card) {
			this.card = card;
		}

		@Override
		public void onPacket(PcapPacket packet) {
			try {
				Packet p = wrap(packet);
				if (p != null)
					card.receivePacket(p);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override	
	public PcapDumper openDump(String filename, boolean isAppend) {		
		Pointer ptr = pcap.isOpened() ? pcap.dumpOpen(filename, isAppend) : null;			
		PcapDumper dumper = new PcapDumper(filename, ptr, pcap.timeUnit());
		
		this.onStop((pcap, value) -> {
			dumper.close();
		});
		
		if (!pcap.isOpened()) {			
			this.onStart((card, value)->{
				// open on start
				Pointer pp = card.pcap.dumpOpen(filename, isAppend);
				dumper.setDumper(pp);
			});			
		}
		
		return dumper;
		
	}

}
