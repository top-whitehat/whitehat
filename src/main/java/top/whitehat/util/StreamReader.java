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
package top.whitehat.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** A Reader that supports both binary and text reading from a stream */
public final class StreamReader extends BufferedInputStream {
	protected static final byte CR = '\r'; // 13
	protected static final byte LF = '\n'; // 10

	private byte[] buf; // internal used buffer
	private int pos; // current position in the buffer
	private int limit; // length of valid bytes in the buffer
	protected Charset charset; // charset of the text

	/** Constructor: create from an InputStream */
	public StreamReader(InputStream in) {
		this(in, StandardCharsets.UTF_8, 8192);
	}

	/**
	 * Constructor: create from an InputStream with specified charset, buffer size
	 */
	public StreamReader(InputStream in, Charset charset, int bufferSize) {
		super(in);
		if (bufferSize <= 0)
			throw new IllegalArgumentException("bufferSize > 0 required");
		if (charset == null)
			charset = StandardCharsets.UTF_8;
		this.in = new BufferedInputStream(in, bufferSize);
		this.charset = charset;
		this.buf = new byte[bufferSize];
		this.pos = 0;
		this.limit = 0;
	}

	/** get charset */
	public Charset getCharset() {
		return this.charset;
	}
	
	/** Test whether it is timeout now */
	private boolean isTimeout(long startTime, int timeout) {
		if (timeout < 0) 
			return false;
		
		if ((System.currentTimeMillis() - startTime)<timeout)
			return false;
		
		return true;
	}
	
	/** wait a while for data incoming 
	 * @throws IOException */
	public void waitForData(int timeoutMs) throws IOException {
		long startTime = System.currentTimeMillis();

		// while there is no data
		while (available() == 0 && !isTimeout(startTime, timeoutMs)) {
			sleep(10); // wait a while
		}
		
		if (isTimeout(startTime, timeoutMs))
			throw new IOException("Wait for data timeout");
		
	}
	
	/** Read a line
	 * 
	 * @return return String of line. return null when end of stream is met.
	 * @throws IOException
	 */
	public String readLine() throws IOException {
		return readLine(-1);
	}
	
	/** Read a line with timeout limit
	 * 
	 * @param timeout   Timeout in milliseconds.
	 * 
	 * @return return String of line. return null when end of stream is met.
	 * @throws IOException
	 */
	public String readLine(int timeout) throws IOException {
		try (ByteArrayOutputStream lineBuf = new ByteArrayOutputStream(256)) {
			int b;
			boolean sawCR = false;
			long startTime = System.currentTimeMillis();

			b = readByte();
			
			while (b != -1 && !isTimeout(startTime, timeout)) {
				if (b == LF) { // \n end of a line
					break;
				}

				if (sawCR) { // previous char is \r, current is not \n
					unreadByte(); // unread \r
					break;
				}

				if (b == CR) { // current is \r
					sawCR = true;
					b = readByte(); // read next char
					continue; 
				}

				// for common char, put it into line buffer
				lineBuf.write(b);
				b = readByte(); // read next char
			}
			
			return lineBuf.size() == 0 && b == -1 ? null : lineBuf.toString(charset.toString());
		}
	}
	
	/** sleep a while */
	private void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
		}
	}

	/** read a byte to buffer, return -1 if the end of the stream is reached (EOF) */
	public int readByte() throws IOException {
		if (pos >= limit) { // bytes in buffer is all used
			// if not has data, exit
			if (this.available() == 0) {
				return -1;
			}
			
			// read from input stream
			limit = in.read(buf);
			pos = 0;
			
			if (limit == -1)
				return -1;
		}
		return buf[pos++] & 0xFF; // convert to unsigned byte
	}

	/** put the byte back to the buffer */
	public void unreadByte() {
		if (pos > 0)
			pos--;
	}

	/**
	 * Peeks at the next byte without advancing the read position.
	 *
	 * @return the next byte as an integer in the range 0..255, or **-1** if the end
	 *         of the stream is reached (EOF)
	 *         
	 * @throws IOException if an I/O error occurs
	 */
	public int peek() throws IOException {
		int b = readByte(); // read a byte
		if (b != -1) {
			unreadByte(); // unread, so, pos is unchanged
		}
		return b;
	}

	/** Check whether there is data for reading */
	public boolean hasData() {
		try {
			return peek() != -1;
		} catch (IOException e) {
			return false;
		}
	}

	/** wait for specified byte values */
	public void waitForValues(byte[] values) throws IOException {
		int b;
		int count = 0;
		int len = values.length;

		b = readByte();
		while (b != -1 && count < len) {
			if (b == (values[count] & 0xFF)) {
				count++;
				if(count >= len) break; 
				b = readByte();
			} else {
				break;
			}
		}

		if (count != len) throw new IOException("value bytes not match");
	}
	
	/** Read to buffer, return -1 if the end of the stream is reached (EOF) */
	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}
	
	/** Read to buffer, return -1 if the end of the stream is reached (EOF) */
	public int read(byte[] buffer, int offset, int len) throws IOException {
		int b;
		int count = 0;

		while ((b = readByte()) != -1 && count < buffer.length) {
			buffer[count++] = (byte) b;
			if (count == len) break;
		}
		
		return count;
	}

}