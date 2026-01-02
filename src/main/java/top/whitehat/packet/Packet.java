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
package top.whitehat.packet;

import top.whitehat.util.ByteArray;

/**
 * A packet
 */
public class Packet extends BasePacket {

	/** packet processor */
	protected PacketProcessor _processor;

	/** Create a empty Packet */
	protected Packet() {
		super();
	}

	/** Create Packet by reading specified data bytes */
	protected Packet(byte[] data) {
		super(data);
	}

	/** Get packet processor */
	public PacketProcessor processor() {
		return _processor;
	}

	/** Set packet processor */
	public Packet processor(PacketProcessor p) {
		_processor = p;
		return this;
	}

	/** Get parent packet */
	public Packet parent() {
		return parent(Packet.class);
	}

	/** Get parent packet as specified class */
	@SuppressWarnings("unchecked")
	public <T> T parent(Class<T> cls) {
		if (_parent == null)
			return null;
		if (cls.isInstance(_parent))
			return (T) _parent;
		throw new RuntimeException("cannot cast to " + cls.getSimpleName());
	}

	/** Set parent packet */
	public Packet parent(Packet p) {
		_parent = p;
		return this;
	}

	/**
	 * Create child packet of specified class from the payload
	 * 
	 * @param clazz  The class of child packet
	 * @param index  The start index of data byte of the child packet
	 * @param length The length of data of the child packet
	 * 
	 * @return child packet
	 */
	@Override
	public <T extends ByteArray> T child(Class<T> clazz, int index, int length) {
		try {
			T p = clazz.getDeclaredConstructor().newInstance();
			int off = arrayOffset() + headerLength() + index;
			p.array(this.array(), off, length);
			p.parent(this);
			// if p is an IMessage, parse message
			if (p instanceof IMessage) {
				((IMessage) p).parseMessage();
			}
			return p;
		} catch (Exception e) {
			throw new PacketException(
					"error create child packet : " + e.getClass().getSimpleName() + " " + e.getMessage());
		}
	}

	/**
	 * Create child packet of specified class from the payload
	 * 
	 * @param clazz The class of child packet
	 * 
	 * @return child packet
	 */
	public <T extends ByteArray> T child(Class<T> clazz) {
		return child(clazz, 0, payloadLength());
	}

	/**
	 * Create child packet from the payload depend on the protocol of payload.
	 * Subclass should implement this method to support upper-level protocols.
	 * 
	 * @return child packet
	 */
	public Packet child() {
		return this;
	}

	/** Initialize the packet's byte array buffer to the specified size */
	public Packet init(int size) {
		capacity(size);
		return this;
	}

	/** Get checksum */
	public long checksum() {
		return 0;
	}

	/**
	 * Set or calculate checksum
	 * 
	 * @param value if the value is zero, means calculate the checksum
	 * @return this
	 */
	public Packet checksum(long value) {
		// subclass should calculation checksum value and write it to buffer
		// ...
		// call parent().checksum(0) to update checksum of parent()
		if (parent() != null)
			parent().checksum(0);
		return this;
	}

	protected static boolean isLayer7PacketClass(Class<?> clazz) {
		while (clazz != null) {
			if (Layer7Packet.class.equals(clazz))
				return true;
			clazz = clazz.getSuperclass();
		}
		return false;
	}

	/** Get packet object of specified class */
	@SuppressWarnings("unchecked")
	public <T extends Packet> T getPacket(Class<T> clazz) {
		Packet p = this;

		// if this class is match
		if (clazz.isInstance(p)) {
			return (T) p;
		}

		// if layer
		if (p instanceof Layer2Packet) {
			p = ((Layer2Packet) p).child();

		} else if (p instanceof Layer3Packet) {
			p = ((Layer3Packet) p).child();

		}

		// for Layer7
		if (isLayer7PacketClass(clazz)) {
			Class<?> thisClass = this.getClass();

			if (UdpPacket.class.equals(thisClass)) {
				return p.child(clazz);
			}

			if (TcpPacket.class.equals(thisClass)) {
				return p.child(clazz);
			}
		}

		// find parent()
		while (p != null && p.parent() != null) {
			p = p.parent();
			if (clazz.isInstance(p))
				return (T) p;
		}

		return null;
	}

	/** Get packet object of specified class with specified port */
	public <T extends Packet> T getPacket(Class<T> clazz, int port) {

		if (Layer7Packet.class.equals(clazz.getSuperclass())) {
			// find udp and port
			UdpPacket udp = this.getPacket(UdpPacket.class);
			if (udp != null) {
				if (udp.srcPort() == port || udp.dstPort() == port) {
					return (T) udp.child(clazz);
				} else {
					return null;
				}
			}

			// find tcp and port
			TcpPacket tcp = this.getPacket(TcpPacket.class);
			if (tcp != null) {
				if (tcp.srcPort() == port || tcp.dstPort() == port) {
					return (T) tcp.child(clazz);
				}
			}
		}
		return null;
	}

}
