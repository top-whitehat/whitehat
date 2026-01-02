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
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.SocketFactory;


/** A base class for TCP client */
public class TcpClient {

	/** Server name */
	private String host;
	
	/** TCP port */
	private int port;

	/** timeout */
	private int timeout = 3000;

	/** Socket factory */
	private SocketFactory socketFactory;

	/** The socket */
	private Socket socket;

	/** input stream reader */
	protected TcpReader reader;

	/** output stream writer */
	protected TcpWriter writer;

	/** Last error message */
	private Exception lastException = null;

	/** Constructor: create a client of TCP */
	public TcpClient() {
	}

	/** Set last exception */
	protected void setLastException(Exception e) {
		lastException = e;
	}

	/** Get last exception */
	public Exception getLastException() {
		return lastException;
	}

	/** throw IOException */
	protected void throwIOException(Exception e) throws IOException {
		throwIOException(e, null);
	}

	/** throw IOException */
	protected void throwIOException(String message) throws IOException {
		throwIOException(null, message);
	}

	/** throw IOException */
	protected void throwIOException(Exception e, String message) throws IOException {
		String cls = (e != null) ? e.getClass().getSimpleName() + " " : "";
		String errMsg = e == null ? "" : e.getMessage();
		String msg = cls + errMsg + (message == null ? "" : message);
		setLastException(e);
		throw new IOException(msg);
	}

	/** Get the host */
	public String getHost() {
		return host;
	}

	/** Set the host */
	public void setHost(String host) {
		this.host = host;
		this.port = getDefaultPort();
	}

	/** Get the port */
	public int getPort() {
		return port;
	}

	/** Set the port */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Get default port, subclass should override this method to set the default
	 * port
	 */
	public int getDefaultPort() {
		return 0;
	}

	/** Get the timeout (in milliseconds) */
	public int getTimeout() {
		return timeout;
	}

	/** Set the timeout (in milliseconds) */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/** Open connection */
	public boolean connect(String host, int port) throws IOException {
		setHost(host);
		setPort(port);
		return connect();
	}
	
	/** Open connection */
	public boolean connect() throws IOException {
		if (socketFactory == null)
			socketFactory = SocketFactory.getDefault();

		if (socket != null)
			disconnect();

		if (socket == null) {
			socket = socketFactory.createSocket();
			socket.connect(new InetSocketAddress(host, port), timeout);
			socket.setSoTimeout(timeout);
			reader = new TcpReader(socket.getInputStream());
			writer = new TcpWriter(socket.getOutputStream());
		}
		
		return socket.isConnected();
	}

	/** Check connection status */
	public boolean isConnected() {
		return socket != null && socket.isConnected();
	}

	/** Close connection */
	public void disconnect() {
		try {
			if (reader != null)
				reader.close();
		} catch (Exception ignore) {
		} finally {
			reader = null;
		}

		try {
			if (writer != null)
				writer.close();
		} catch (Exception e) {
		} finally {
			writer = null;
		}

		try {
			if (socket != null)
				socket.close();
		} catch (Exception ignore) {
		} finally {
			socket = null;
		}
	}
	
	/** Get writer for output */
	protected TcpWriter getWriter() {
		return writer;
	}
	
	/** get reader for input */
	protected TcpReader getReader() {
		return reader;
	}
	
	/** Sleep a while */
	protected void sleep(int timeMilliseconds) {
		try {
			Thread.sleep(timeMilliseconds);
		} catch (InterruptedException e) {
		}
	}
	
	/** Send string to the server, do not read response immediately */
	protected void send(String str) throws IOException {
		writer.print(str);
		writer.flush();
	}
	
	/** Send data bytes to the server, do not read response immediately */
	protected void send(byte[] data) throws IOException {
		writer.write(data);
		writer.flush();
	}
	
	/** Returns an estimate of the number of bytes that can be read  */
	protected int available() {
		try {
			return reader.available();
		} catch (IOException e) {
			return 0;
		}
	}
	
	/**
	 * Reads bytes from the server into the specified buffer,starting at the given offset. 
	 * This method implements the general contract of the corresponding read method ofthe InputStream class. As an additionalconvenience, it attempts to read as many bytes as possible by repeatedlyinvoking the read method of the underlying stream. Thisiterated read continues until one of the followingconditions becomes true: 
		• The specified number of bytes have been read, 
		• The read method of the underlying stream returns -1, indicating end-of-file, or 
		• The available method of the underlying stream returns zero, indicating that further input requests would block. 

	 * @param buffer   The byte array buffer
	 * @param offset   Offset at which to start storing bytes.
	 * @param len      Maximum number of bytes to read.
	 * @return
	 * @throws IOException
	 */
	protected int read(byte[] buffer, int offset, int len) throws IOException {
		return reader.read(buffer, offset, len);
	}
	
	protected int read(byte[] buffer) throws IOException {
		return reader.read(buffer);
	}
	
	/** Read a line from the server, do not return until line ends */
	protected String readLine() throws IOException {
		return reader.readLine();
	}
	
	/** Wait for specific pattern in server response */
	protected String readLine(int timeoutMs) throws IOException {
		return reader.readLine(timeoutMs);
	}
	
	/** wait for specified byte values */
	protected void waitForValues(byte[] values) throws IOException {
		reader.waitForValues(values);
	}
	
	/** Wait for data arrive */
	protected void waitForData(int timeoutMs) throws IOException {
		getReader().waitForData(timeoutMs);
	}
	
	/** Wait for specific pattern in server response */
	protected String waitFor(String pattern, int timeoutMs) throws IOException {
		StringBuilder response = new StringBuilder();
		byte[] buffer = new byte[4096];
		long startTime = System.currentTimeMillis();

		while (System.currentTimeMillis() - startTime < timeoutMs) {
			if (reader.hasData()) {
				int bytesRead = reader.read(buffer);
				if (bytesRead > 0) {
					String chunk = new String(buffer, 0, bytesRead, reader.getCharset());
					response.append(chunk);
					String ss = response.toString();
					if (ss.contains(pattern)) {
						return response.toString();
					}
				}
			} else {
				sleep(50);
			}

		}

		String msg = "Timeout waiting for pattern: " + pattern;
		throwIOException(new RuntimeException(msg));
		return null;
	}
	
}
