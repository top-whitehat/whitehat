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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Output stream for response data in TCP server */
public class TcpOutputStream extends OutputStream {
	
	protected Charset charset =  StandardCharsets.UTF_8;
	
	byte[] buffer = new byte[2];
	
	int limit = 0;

	/** return the buffer */
	public byte[] getBuffer() {
		return buffer;
	}
	
	/** return limit */
	public int limit() {
		return limit;
	}
	
	/** return available bytes */
	public int available() {
		return limit; // - position;
	}
	
	/** return size of the buffer */
	public int capacity() {
		return buffer.length;
	}
	
	/** Ensure the capacity is larger than specified minCapacity, 
	 *  enlarge the buffer if needed. */
	public void ensureCapcity(int minCapacity) {
		if (capacity() >= minCapacity) return;
		minCapacity += (minCapacity % 64);  // 64-byte align		
		byte[] newBuffer = new byte[minCapacity]; // create new ByteBuffer
		System.arraycopy(buffer, 0, newBuffer, 0, buffer.length); // copy bytes from original buffer
		buffer = newBuffer; // change buffer to the new one.
	}		
	
	@Override
	public void write(int b) throws IOException {
		ensureCapcity(limit + 1);
		buffer[limit++] = (byte)(b & 0xFF);
	}
	
	public ByteBuffer toByteBuffer() {
		return ByteBuffer.wrap(buffer, 0, limit);
	}
	
	/** Keep the last n bytes of the byte array and move them to the 
	 *  beginning of the array, discarding the other bytes. */
	public void remain(int nBytes)  throws IOException {
		if (nBytes <= 0) return;
		int pos = limit - nBytes;
		if (pos <= 0) return;
		
		for(int i=0; i<nBytes; i++) {
			buffer[i] = buffer[pos+i];
		}
		
		limit = nBytes;
	}

	public void flush() throws IOException {
		limit = 0;
	}

	public void close() throws IOException {
    }
	
	/** print a string */
    public void print(String str) throws IOException {    	
    	write(str.getBytes(charset));
    }
    
    /** print a line  */
    public void println(String str) throws IOException {
    	print(str);
    	print("\r\n");
    }
    
}
