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

import com.sun.jna.Pointer;

import top.whitehat.pcap.PcapLib.pcap_stat;
import top.whitehat.pcap.PcapLib.win_pcap_stat;

/** packet statistics */
public final class PcapStat {

	/** packet received count */
	private final long receivedCount;

	/** packet dropped count */
	private final long droppedCount;

	/** packet droppedByIf count */
	private final long droppedByIfCount;

	/** packet captured count */
	private final long capturedCount;

	public PcapStat(pcap_stat stat) {
		if (stat instanceof win_pcap_stat) {
			win_pcap_stat win_stat = (win_pcap_stat) stat;
			this.receivedCount = win_stat.ps_recv & 0xFFFFFFFFL;
			this.droppedCount = win_stat.ps_drop & 0xFFFFFFFFL;
			this.droppedByIfCount = win_stat.ps_ifdrop & 0xFFFFFFFFL;
			this.capturedCount = win_stat.bs_capt & 0xFFFFFFFFL;
		} else {
			this.receivedCount = stat.ps_recv & 0xFFFFFFFFL;
			this.droppedCount = stat.ps_drop & 0xFFFFFFFFL;
			this.droppedByIfCount = stat.ps_ifdrop & 0xFFFFFFFFL;
			this.capturedCount = 0;
		}
	}

	public PcapStat(Pointer p, boolean isWinPcapStat) {
		this.receivedCount = pcap_stat.getPsRecv(p) & 0xFFFFFFFFL;
		this.droppedCount = pcap_stat.getPsDrop(p) & 0xFFFFFFFFL;
		this.droppedByIfCount = pcap_stat.getPsIfdrop(p) & 0xFFFFFFFFL;
		
		if (isWinPcapStat) {
			this.capturedCount = win_pcap_stat.getBsCapt(p) & 0xFFFFFFFFL;
		} else {
			this.capturedCount = 0;
		}
	}

	/** return packet received count */
	public long getPacketsReceived() {
		return receivedCount;
	}

	/** return packet dropped count */
	public long getPacketsDropped() {
		return droppedCount;
	}

	/** return packet dropped count */
	public long getPacketsDroppedByIf() {
		return droppedByIfCount;
	}

	/** return packet captured count */
	public long getPacketsCaptured() {
		return capturedCount;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[");
		sb.append("Received=").append(receivedCount);
		sb.append(", Dropped=").append(droppedCount);
		sb.append(", Captured=").append(capturedCount);
		sb.append(", DroppedByIfCount=").append(droppedByIfCount);
		sb.append("]");
		return sb.toString();
	}
}
