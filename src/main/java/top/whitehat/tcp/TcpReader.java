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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** A Reader that supports both binary and text reading from a InputStream */
public final class TcpReader extends BufferedInputStream {
	protected static final byte CR = '\r'; // 13
	protected static final byte LF = '\n'; // 10

	private byte[] buffer; // internal used buffer
	private int position; // current position in the buffer
	private int limit; // length of valid bytes in the buffer
	private Charset charset; // charset of the text
	private InputStream inStream;

	/** Constructor: create from an InputStream */
	public TcpReader(InputStream in) {
		this(in, StandardCharsets.UTF_8, 8192);
	}

	/**
	 * Constructor: create from an InputStream with specified charset, buffer size
	 */
	public TcpReader(InputStream in, Charset charset, int bufferSize) {
		super(in);
		this.inStream = in;
		if (bufferSize <= 0)
			throw new IllegalArgumentException("bufferSize > 0 required");
		if (charset == null)
			charset = StandardCharsets.UTF_8;
		this.in = new BufferedInputStream(in, bufferSize);
		this.charset = charset;
		this.buffer = new byte[bufferSize];
		this.position = 0;
		this.limit = 0;
	}

	public byte[] remain() {
		int len = limit - position;
		byte[] ret = new byte[len];
		System.arraycopy(buffer, position, ret, 0, len);
		return ret;
	}

	public String remainStr() {
		return new String(remain(), charset);
	}

	/** get charset */
	public Charset getCharset() {
		return this.charset;
	}
	
	/** set charset */
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	/** Test whether it is timeout now */
	private boolean isTimeout(long startTime, int timeout) {
		if (timeout < 0)
			return false;

		if ((System.currentTimeMillis() - startTime) < timeout)
			return false;
		else
			return true;
	}

	/**
	 * wait a while for data incoming
	 * 
	 * @throws IOException
	 */
	public void waitForData(int timeoutMs) throws IOException {
		if (position >= limit) { // if no data in buffer
			ExecutorService executor = Executors.newSingleThreadExecutor();
			// compose a task
			Future<?> future = executor.submit(() -> {
				try {
					// maybe time-consuming
					int b = readByte();
					if (b != -1)
						unreadByte();
				} catch (IOException e) {
				}
			});

			try {
				// Wait for the task to complete, but no longer than 5 seconds
				future.get(timeoutMs, TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				throw new IOException("Wait for data timeout");
			} catch (Exception e) {
				// Handle other exceptions
				throw new IOException(e.getClass().getSimpleName() + " " + e.getMessage());
			} finally {
				future.cancel(true); // Interrupt the task if it's still running
				executor.shutdown();
			}

		}

//		long startTime = System.currentTimeMillis();
//		
//		// while there is no data
//		while (!hasData() && !isTimeout(startTime, timeoutMs)) {
//			sleep(10); // wait a while
//		}
//		
//		if (isTimeout(startTime, timeoutMs)) {
//			String msg = "available = " + super.available();
//			msg += ", position=" + position + ", limit=" + limit;
//			throw new IOException("Wait for data timeout " + msg);
//		}

	}

	/** sleep a while */
	public static void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * read a byte to buffer, return -1 if the end of the stream is reached (EOF)
	 */
	public int readByte() throws IOException {
		if (position >= limit) { // bytes in buffer is all used
//			// if not has data, exit
//			if (super.available() == 0) {
//				return -1;
//			}

			// read from input stream
			limit = in.read(buffer);
			position = 0;

			if (limit == -1)
				return -1;
		}

		return buffer[position++] & 0xFF; // convert to unsigned byte
	}

	/** Pushes current byte back to the buffer */
	public void unreadByte() {
		if (position > 0)
			position--;
	}

	/**
	 * Pushes back a portion of an array of bytes by copying it to the front of the
	 * buffer. After this method returns, the next byte to be read will have the
	 * value <code>b[off]</code>, the byte after that will have the value
	 * <code>b[off+1]</code>, and so forth.
	 * 
	 * @param b   the byte array to push back.
	 * @param off the start offset of the data.
	 * @param len the number of bytes to push back.
	 * 
	 * @throws IOException IOException If there is not enough room in the pushback
	 *                     buffer
	 */
	public void unread(byte[] b, int off, int len) throws IOException {
		if (len == 0)
			return;

		if (position >= len) {
			position -= len;
			System.arraycopy(b, off, buffer, position, len);
		} else {
			int remain = limit - position;
			if (len + remain > buffer.length) {
				throw new IOException("error unread: data exceed the buffer");

			} else {
				// backup remain bytes
				byte[] remainBytes = new byte[remain];
				System.arraycopy(buffer, position, remainBytes, 0, remain);

				// copy unread bytes
				position = 0;
				limit = len + remain;
				System.arraycopy(b, off, buffer, position, len);

				// restore remain bytes
				System.arraycopy(remainBytes, 0, buffer, len, remain);
			}
		}
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
	
	/** Return count of available bytes*/
	public void receiveData(ByteBuffer buffer) {
		if (inStream instanceof TcpInputStream) {
			((TcpInputStream)inStream).append(buffer);
		} else {
			throw new RuntimeException("cannot append data to input stream");
		}
	}
	
	/** Check whether there is data for reading */
	public boolean hasData() {
		try {
			return peek() != -1;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Read a line
	 * 
	 * @return return String of line. return null when end of stream is met.
	 * @throws IOException
	 */
	public String readLine() throws IOException {
		return readLine(-1);
	}

	/**
	 * Read a line with timeout limit
	 * 
	 * @param timeout Timeout in milliseconds.
	 * 
	 * @return return String of line. return null when end of stream is met.
	 * @throws IOException
	 */
	public String readLine(int timeout) throws IOException {
		ByteArrayOutputStream lineBuf = new ByteArrayOutputStream(256);
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

	/** Read to buffer, return -1 if the end of the stream is reached (EOF) */
	public int read(byte[] data) throws IOException {
		return read(data, 0, data.length);
	}

	/** Read to buffer, return -1 if the end of the stream is reached (EOF) */
	public int read(byte[] data, int offset, int len) throws IOException {
		int b;
		int count = 0;

		while ((b = readByte()) != -1 && count < data.length) {
			data[count++] = (byte) b;
			if (count == len)
				break;
		}

		return count;
	}

	/**
	 * Read to buffer, until specified delimiter is met
	 * 
	 * @param data
	 * @param delimiter
	 * @param timeoutMs
	 * @return
	 * @throws IOException
	 */
	public int readUntil(byte[] data, byte[] delimiter, int timeoutMs) throws IOException {
		Objects.requireNonNull(data);
		Objects.requireNonNull(delimiter);

		int bufferLength = data.length;
		int delimLength = delimiter.length;
		if (data.length == 0)
			throw new IOException("data buffer is zero-length");
		if (delimiter.length == 0)
			throw new IOException("delimiter is zero-length");
		if (bufferLength <= delimLength)
			throw new IOException("data buffer length should larger than delimiter length");

		int count = 0;
		int match = 0;
		int b;

		// read a byte
		b = readByte();

		while (b != -1) {

			if (match == 0) { // there are no matched bytes
				// try to match the first byte
				if (b == (delimiter[0] & 0xFF)) {
					if (count + delimLength > bufferLength) { // buffer no more spaces
						unreadByte();
						break;
					} else {
						match = 1;
					}

				} else {
					// For not-match bytes, add to the data buffer
					data[count++] = (byte) b;
					if (count == bufferLength)
						break;
				}

			} else { // there are matched bytes
				// try to match the next byte
				if (b == (delimiter[match] & 0xFF)) {
					// increase match count
					match++;
					if (match == delimLength) {
						return -count;
					}

				} else {
					// write matched byte to data
					System.arraycopy(delimiter, 0, data, count, match);
					unreadByte();
					count += match;
					break;
				}
			}

			// read next byte
			b = readByte();
		}

		return count;
	}

	/**
	 * Read delimiter at current position
	 * 
	 * @return return count of bytes that match the delimiter. if the return value
	 *         is same as the length of the delimiter, then the delimiter is found.
	 * 
	 * @throws IOException
	 */
	public int readDelimiter(byte[] delimiter, int start) throws IOException {
		int b;
		int count = start;
		int len = delimiter.length;

		b = readByte();

		while (b != -1) {
			if (b == (delimiter[count] & 0xFF)) {
				count++;
				if (count == len)
					break;

				b = readByte();
			} else {
				unreadByte();
				break;
			}
		}

		return count;
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
				if (count >= len)
					break;
				b = readByte();
			} else {
				break;
			}
		}

		if (count != len)
			throw new IOException("value bytes not match");
	}

}