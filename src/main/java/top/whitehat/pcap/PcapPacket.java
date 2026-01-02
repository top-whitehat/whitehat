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

import java.time.Instant;
import java.util.Arrays;

/** ​​PcapPacket is a wrapper for the raw bytes of a captured packet.​ */
public class PcapPacket {

	private byte[] rawData;

	private Instant timestamp;

	private int originalLength;

	private int datalinkType;

	public PcapPacket(byte[] rawData, int datalinkType, Instant timestamp, int originalLength) {
		this.rawData = rawData;
		this.timestamp = timestamp;
		this.originalLength = originalLength;
		this.datalinkType = datalinkType;
	}

	/** Return the timestamp of when this packet was captured. */
	public Instant getTimestamp() {
		return timestamp;
	}

	/** Return data link type */
	public int getDatalinkType() {
		return datalinkType;
	}

	/** Return the original length of this packet. */
	public int getOriginalLength() {
		return originalLength;
	}

	/** Get the length of the captured packet. */
	public int length() {
		return rawData.length;
	}

	/** Get the raw data of the captured packet. */
	public byte[] getRawData() {
		return rawData;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PcapPacket packets = (PcapPacket) o;

		if (originalLength != packets.originalLength) {
			return false;
		}

		if (!timestamp.equals(packets.timestamp)) {
			return false;
		}

		return Arrays.equals(rawData, packets.rawData);
	}

	@Override
	public int hashCode() {
		int result = 0; // super.hashCode();
		result = 31 * result + Arrays.hashCode(rawData);
		result = 31 * result + timestamp.hashCode();
		result = 31 * result + originalLength;
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		if (rawData != null) {
			sb.append(" [time=").append(timestamp);
			sb.append(" length=").append(rawData.length).append("] ");
			int len = rawData.length;
			for (int i = 0; i < len; i++) {
				sb.append(String.format("%02x ", rawData[i] & 0xFF));
			}
		}
		return sb.toString();
	}
}
