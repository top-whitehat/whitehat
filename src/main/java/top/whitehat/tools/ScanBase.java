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
import java.util.ArrayList;
import java.util.List;

import top.whitehat.NetCard;
import top.whitehat.NetEventArgs;
import top.whitehat.NetEventListener;
import top.whitehat.PacketListener;
import top.whitehat.packet.MacAddress;

/** Base class for ScanXXX class */
public abstract class ScanBase implements PacketListener {
	
	/** default speed in packet per second */
	public static int DEFAULT_SPEED = 5000;

	/** The network interface card */
	protected NetCard netCard;

	/** InetAddress of the NetCard */
	protected InetAddress srcAddress;

	/** Mac Address of the NetCard */
	protected MacAddress srcMac;

	/** How many retry times */
	protected int retryTimes = 0;

	/** Finished retry times */
	protected int retryCount = 0;

	/** whether the scan task is canceled */
	protected boolean canceled = false;

	/** options */
	protected int options = 0;

	/** speed: how many packets/items can be processed per second, default is 1000  */
	protected int _speed = DEFAULT_SPEED;

	/** how many steps in a second. sleep between steps */
	protected int stepPerSecond = 5;

	/** Listeners for when report scan progress */
	protected List<NetEventListener> progressListeners = new ArrayList<NetEventListener>();

	/** Listeners for when an Ip is found */
	protected List<NetEventListener> findListeners = new ArrayList<NetEventListener>();

	/**
	 * Constructor: create an instance on default NetCard that is connected to the
	 * Internet
	 */
	public ScanBase() {
		this(NetCard.inet());
	}

	/** Constructor: create an instance on specified NetCard */
	public ScanBase(NetCard netCard) {
		if (netCard == null)
			throw new IllegalArgumentException("NetCard is null, cannot find");

		this.netCard = netCard;
		this.srcAddress = this.netCard.ip();
		this.srcMac = this.netCard.mac();
		this.netCard.onPacket(p -> {
			this.onPacket(p);
		});
	}
	
	public InetAddress srcIp() {
		return srcAddress;
	}
	
	public ScanBase srcIp(InetAddress value) {
		srcAddress = value;
		return this;
	}
	
	public MacAddress srcMac() {
		return srcMac;
	}
	
	public ScanBase srcMac(MacAddress value) {
		srcMac = value;
		return this;
	}

	/** Get the speed: packets(or items) per second */
	public int speed() {
		return _speed;
	}

	/** Set the speed: packets(or items) per second */
	public ScanBase speed(int value) {
		_speed = value;
		return this;
	}

	/** Get retry times */
	public int retry() {
		return retryTimes;
	}

	/** Set retry times */
	public ScanBase retry(int value) {
		retryTimes = value;
		return this;
	}

	/** Get options */
	public int options() {
		return options;
	}

	/** Set options */
	public ScanBase options(int value) {
		options = value;
		return this;
	}

	/** Check whether specified option exists */
	protected boolean hasOption(int option) {
		return (options & option) > 0;
	}

	/** Return the NetCard that binds to this object */
	public NetCard getNetCard() {
		return this.netCard;
	}

	/** clear */
	public ScanBase clear() {
		retryCount = 0;
		return this;
	}

	/** Do progress report.
	 * 
	 * Sleep a while, and then Trigger onProgress() event.
	 * 
	 * @param i         The index of current work
	 * @param count     The count of items
	 * @param startTime The starting time in tick count(milliseconds)
	 */
	protected void doProgress(int i, long count, long startTime) {
		// step indicates the number of items processed before generate a report.
		int step = _speed / stepPerSecond;
		if (step < 1)
			step = 1;

		// when step is met, sleep a while
		if ((i > 0) && (i % step) == 0) {
			try {
				long previousFinished = retryCount * count; // previous finished count
				long currentFinished = previousFinished + i; // current finished count
				long total = (retryTimes + 1) * count; // total ip count

				triggerOnProgress(currentFinished, total);

				long timeUage = System.currentTimeMillis() - startTime;
				long timeDelta = (long) ((currentFinished * 1.0 / _speed) * 1000 - timeUage);
				if (timeDelta > 10) {
					Thread.sleep(timeDelta);
				}
			} catch (InterruptedException e) {
			}
		}
	}

	/** Trigger onProgress event */
	protected void triggerOnProgress(long count, long total) {
		// trigger onProgress event
		ScanProgress p = new ScanProgress(count, total, "");
		for (NetEventListener l : progressListeners) {
			l.onEvent(new NetEventArgs(this, null, p));
		}
	}

	/** Trigger onFind event */
	protected void triggerOnFind(InetAddress addr, Object value) {
		// trigger onFind event
		for (NetEventListener l : findListeners) {
			l.onEvent(new NetEventArgs(this, addr, value));
		}
	}
	
	/** Add a listener to handle progress updates */
	public ScanBase onProgress(NetEventListener l) {
		progressListeners.add(l);
		return this;
	}

	/** Register a callback for IP address detection */
	public ScanBase onFind(NetEventListener l) {
		findListeners.add(l);
		return this;
	}
	
	/** stop scan */
	public void stop() {
		canceled = true;
		netCard.stop();
	}


}
