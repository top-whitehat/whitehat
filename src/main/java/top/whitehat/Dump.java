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

import java.time.Instant;

import top.whitehat.packet.Packet;

public interface Dump {

	/** write packet data bytes with specified timestamp to dump file */
	public void write(byte[] packet, Instant timestamp);
	 
	/** write packet data bytes to dump file */
	public default void write(byte[] packet) {
		write(packet, Instant.now());
	}
	
	/** write packet to dump file */
	public default void write(Packet packet, Instant timestamp) {
		write(packet.array(), timestamp);
	}
	
	/** write packet  to dump file */
	public default void write(Packet packet) {
		write(packet.array(), Instant.now());
	}
	
	/** dump flush */
	public void flush();
	
	
	/** closes the dumper. */
	public void close();
	 
}
