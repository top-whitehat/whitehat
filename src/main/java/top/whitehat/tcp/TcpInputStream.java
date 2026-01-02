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
package top.whitehat.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/** Input stream for request data in TCP server */
public class TcpInputStream extends InputStream {
	
	/** the back end buffer */
	private byte buffer[] = new byte[0];
	
	private int position = 0;
	
	private int limit = 0;

	/** Constructor */
	public TcpInputStream() {
	}
	
	public int position() {
		return position;
	}
	
	public int limit() {
		return limit;
	}
	
	/** return the buffer */
	public byte[] getBuffer() {
		return buffer;
	}
	
	/** return size of the buffer */
	public int capacity() {
		return buffer.length;
	}

	/** Ensure the capacity is larger than specified minCapacity, 
	 *  enlarge the buffer if needed. */
	public void ensureCapcity(int minCapacity) {
		if (capacity() >= minCapacity)
			return;
		byte[] newBuffer = new byte[minCapacity]; // create new ByteBuffer
		System.arraycopy(buffer, 0, newBuffer, 0, capacity()); // copy bytes from original buffer
		buffer = newBuffer; // change buffer to the new one.
	}

	/** Append bytes from specified byte buffer */
	public void append(ByteBuffer b) {
		append(b, 0, b.limit());
	}

	/** Append bytes of specified length from specified byte buffer at specified index */
	public void append(ByteBuffer b, int index, int length) {
		append(b.array(), index, length);
	}

	/** Append bytes of specified length from specified byte array at specified index  */
	public void append(byte[] bs, int index, int length) {
		ensureCapcity(limit + length);
		System.arraycopy(bs, index, buffer, limit, length);
		limit += length;
	}

	/** Append bytes of specified length from specified stream */
	public void append(InputStream stream, int length) throws IOException {
		ensureCapcity(limit + length);
		int b;
		while ((b = stream.read()) != -1) {
			buffer[limit++] = (byte)b;
		}
	}	

	/**
	 * Compact the buffer.<br> 
	 * 
	 * The bytes between the buffer's current position and its limit,if any, are copied to 
	 * the beginning of the buffer. 
	 */
	public void compact() {
		if (position == 0) return;
		int count = 0;
		for(int i=position; i<limit; i++) {
			buffer[count++] = buffer[i];
		}
	}

	@Override
	public int read() throws IOException {
		if (position < limit) {
			return buffer[position++] & 0xFF;
		}
		return -1;
	}

	@Override
	public int available() throws IOException {
		return limit - position;
	}

	@Override
	public void close() throws IOException {

	}

}
