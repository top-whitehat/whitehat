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
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/** TCP connection */
public class TcpConnection {

	protected TcpServer server;
	private TcpKey tcpKey;

	private boolean keepAlive = true;
	private InputStream inputStream;
	private OutputStream outputStream;
	private TcpReader reader;
	private TcpWriter writer;
	
	private final ReentrantLock processLock = new ReentrantLock();
	private boolean reading = false;
	private boolean writing = false;

	public TcpConnection(TcpServer server, TcpKey tcpKey) throws IOException {
		this.server = server;
		this.tcpKey = tcpKey;
		this.inputStream = new TcpInputStream();
		this.outputStream = new TcpOutputStream();
		this.reader = new TcpReader(inputStream);
		this.writer = new TcpWriter(outputStream);
	}
	
	public void setStreams(InputStream inStream, OutputStream outStream) {
		this.inputStream = inStream;
		this.outputStream = outStream;
		this.reader = new TcpReader(inputStream);
		this.writer = new TcpWriter(outputStream);
	}
	
	public boolean tryLock() {
        return processLock.tryLock();
    }
	
	public void unlock() {
		processLock.unlock();
    }

	/** Return client IP address */
	public InetAddress srcIp() {
		return tcpKey.srcIp;
	}

	/** Return client port */
	public int srcPort() {
		return tcpKey.srcPort;
	}

	/** Return server IP address */
	public InetAddress dstIp() {
		return tcpKey.dstIp;
	}

	/** Return server port */
	public int dstPort() {
		return tcpKey.dstPort;
	}

	/** Indicate whether keep alive */
	public boolean keepAlive() {
		return keepAlive;
	}

	/** Write data to input stream */
	public void receiveData(ByteBuffer buffer) {
		reader.receiveData(buffer);
	}

	/** Indicate whether the output stream has data that need to sent */
	public boolean hasSendingData() {
		return writer.available() > 0;
	}

	/** Indicate whether current status is reading */
	public boolean isReading() {
		return reading;
	}

	/** Indicate whether current status is writing */
	public boolean isWriting() {
		return writing;
	}

	/** Set reading status */
	public void setReading(boolean value) {
		reading = value;
	}

	/** Set writing status */
	public void setWriting(boolean value) {
		writing = value;
	}

	/** Indicate whether keep alive: do not disconnect after response */
	public boolean isKeepAlive() {
		return keepAlive;
	}

	/** Set keep alive status: set true means do not disconnect after response */
	public void setKeepAlive(boolean value) {
		keepAlive = value;
	}

	/** Get output stream */
	public OutputStream getOutputStream() {
		return outputStream;
	}

	/** Get writer for output */
	public TcpWriter getWriter() {
		return writer;
	}

	/** Get writer for input */
	public TcpReader getReader() {
		return reader;
	}

}