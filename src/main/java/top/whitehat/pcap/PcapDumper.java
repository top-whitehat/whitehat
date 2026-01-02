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

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import top.whitehat.Dump;
import top.whitehat.pcap.PcapLib.pcap_pkthdr;
import top.whitehat.pcap.PcapLib.timeval;

import static top.whitehat.pcap.Pcap.*;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class PcapDumper implements Dump, AutoCloseable {

	private Pointer dumper;
	private int _timeUnit;
	private String filename;
	private ReentrantReadWriteLock dumperLock = new ReentrantReadWriteLock(true);

	public PcapDumper(String filename, Pointer dumper, int timestampPrecision) {
		this.filename = filename;
		this.dumper = dumper;
		this._timeUnit = timestampPrecision;		
	}

	public void setDumper(Pointer p) {
		dumper = p;
	}
	
	public Pointer getDumper() {
		return dumper;
	}
	
	public String getFilename() {
		return filename;
	}
	
	/** dump pcap packet */
	public void dump(PcapPacket packet) {
		write(packet.getRawData(), packet.getTimestamp());
	}

	/** dump packet data bytes with specified timestamp */
	public void write(byte[] packet, Instant timestamp) {	
		if (packet == null || timestamp == null || dumper == null) {
			throw new NullPointerException();
		}

		pcap_pkthdr header = new pcap_pkthdr();
		header.len = header.caplen = packet.length;
		header.ts = new timeval();
		header.ts.tv_sec = new NativeLong(timestamp.getEpochSecond());

		switch (_timeUnit) {
		case Pcap.MICROSECONDS:
			header.ts.tv_usec = new NativeLong(timestamp.getNano() / 1000L);
			break;
		case Pcap.NANOSECONDS:
			header.ts.tv_usec = new NativeLong(timestamp.getNano());
			break;
		default:
			throw new AssertionError("Never get here.");
		}

		tryLock();
		try {
			PcapLib.pcap_dump(dumper, header, packet);
		} finally {
			unlock();
		}
	}

	/** dump flush */
	public void flush() {
		int rc;
		
		tryLock();
		try {
			rc = PcapLib.pcap_dump_flush(dumper);
		} finally {
			unlock();
		}

		if (rc < 0) {
			throw new PcapException(rc, "Failed to flush.");
		}
	}
	
	/** Check whether this Pcap object is opened */
	public boolean isOpened() {
		return dumper != null;
	}
	
	/** check whether opened */
	protected void checkOpen() {
		if (!isOpened()) {
			throw new PcapException(ERR_NOT_OPEN, "not open");
		}
	}

	/** try lock handle */
	protected void tryLock() {
		checkOpen();
		if (!dumperLock.readLock().tryLock()) {
			throw new PcapException(ERR_NOT_OPEN, "not open");
		}
		checkOpen();
	}

	/** unlock handle */
	protected void unlock() {
		dumperLock.readLock().unlock();
	}

	/** ftell​​ retrieves the current value of the file position indicator 
	 *  for a given stream
	 * 
	 * @return the file position
	 */
	public long ftell() {
		tryLock();
		
		NativeLong nposition;
		try {
			nposition = PcapLib.pcap_dump_ftell(dumper);
		} finally {
			unlock();
		}

		long position = nposition.longValue();
		if (position < 0) {
			throw new PcapException(ERR_FILE_READ, "Failed to get the file position.");
		}

		return position;
	}

	/** closes this PcapDumper. */
	@Override
	public void close() {
		if (dumper == null) return;
		
		dumperLock.writeLock().lock();
		try {
			if (isOpened()) {
				PcapLib.pcap_dump_close(dumper);
			}
		} finally {
			dumper = null;
			dumperLock.writeLock().unlock();
		}
	}

}
