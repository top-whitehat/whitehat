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
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/** TcpService that using java.nio.channels */
public class TcpNIOService extends TcpService {

	private TcpServer server;
	private ServerSocketChannel serverChannel;
	private Selector selector;

	/** Initialization */
	public TcpService init(TcpServer server) throws IOException {
		this.server = server;

		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverChannel.bind(new InetSocketAddress(server.getPort()));

		selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		return this;
	}

	/** Start service */
	public void start() {
		while (server.isRunning()) {
			try {
				if (selector.select(1000) == 0) {
					continue;
				}

				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectedKeys.iterator();

				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();

					if (!key.isValid())
						continue;

					try {
						if (key.isAcceptable()) {
							handleAccept(key);
						} else if (key.isReadable()) {
							handleRead(key);
						} else if (key.isWritable()) {
							handleWrite(key);
						}
					} catch (IOException e) {
						key.cancel();
						try {
							key.channel().close();
						} catch (IOException ex) {
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** Close service */
	public void close() {
		server.setRunning(false);

		try {
			if (selector != null)
				selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (serverChannel != null)
				serverChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		selector = null;
		serverChannel = null;
	}

	/** Get info from SocketChannel */
	private TcpKey getTcpKey(SocketChannel clientChannel) throws IOException {
		TcpKey key = new TcpKey();

		SocketAddress sAddr;
		InetSocketAddress iAddr;

		sAddr = clientChannel.getLocalAddress();
		if (sAddr instanceof InetSocketAddress) {
			iAddr = (InetSocketAddress) sAddr;
			key.dstIp = iAddr.getAddress();
			key.dstPort = iAddr.getPort();
		}

		sAddr = clientChannel.getRemoteAddress();
		if (sAddr instanceof InetSocketAddress) {
			iAddr = (InetSocketAddress) sAddr;
			key.srcIp = iAddr.getAddress();
			key.srcPort = iAddr.getPort();
		}

		return key;
	}

	/** Handle accept event */
	private void handleAccept(SelectionKey key) throws IOException {
		// get channel
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		SocketChannel clientChannel = serverSocketChannel.accept();
		clientChannel.configureBlocking(false);

		// create connection object and bind it to the selector
		TcpConnection connection = new TcpConnection(server, getTcpKey(clientChannel));
		clientChannel.register(selector, SelectionKey.OP_READ, connection);

		// trigger onConnect event
		server.triggerOnConnect(connection);

		// if data is written in onConnect()
		if (connection.hasSendingData()) {
			clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, connection);
		}
	}

	private int readEventCount = 0;

	private int writeEventCount = 0;

	/** Handle read event */
	private void handleRead(SelectionKey key) throws IOException {
		if (!key.isValid() || !key.isReadable())
			return;
		TcpConnection connection = (TcpConnection) key.attachment();
		System.out.println("read event " + (readEventCount++));
		if (!connection.isReading()) {
			server.execute(new ReceiveThread(server, key));
		}
	}

	/** Handle write event */
	private void handleWrite(SelectionKey key) throws IOException {
		if (!key.isValid() || !key.isWritable())
			return;
		TcpConnection connection = (TcpConnection) key.attachment();
		System.out.println("write event " + (writeEventCount++));
		if (!connection.isWriting() && connection.hasSendingData()) {
			server.execute(new SendThread(server, key));
		}
	}

	/** Set OP_WRITE interest status for key */
	protected static void setOPWrite(SelectionKey key, boolean needWrite) {
		if (key.isValid()) {
			if (needWrite)
				key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			else
				key.interestOps(SelectionKey.OP_READ);
		}
	}

	/** A Thread for receiving data */
	protected static class ReceiveThread implements Runnable {
		private TcpServer server;
		private SelectionKey key;
		private TcpConnection connection;

		public ReceiveThread(TcpServer server, SelectionKey key) {
			this.server = server;
			this.key = key;
			this.connection = (TcpConnection) key.attachment();
			this.connection.setReading(true);
		}

		public TcpServer getServer() {
			return this.server;
		}

		@Override
		public void run() {
			try {
				handleRead();
			} finally {
				connection.setReading(false);
				setOPWrite(key, connection.hasSendingData());
			}
		}

		public void handleRead() {
			System.out.println("session read key=" + key);
			if (!key.isValid() || !key.isReadable())
				return;

			SocketChannel channel = (SocketChannel) key.channel();
			int count = 0;

			try {
				// read incoming data
				ByteBuffer buffer = ByteBuffer.allocate(4096);
				int bytesRead = channel.read(buffer);

				if (bytesRead > 0) {
					count += bytesRead;
					buffer.flip();
					connection.receiveData(buffer);

				} else if (bytesRead == -1) {
					key.cancel();
					channel.close();

				}

			} catch (IOException e) {
				try {
					System.out.println(e);
					channel.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			if (count > 0) {
				server.dataIncome(connection);
			}
		}

	}

	/** A Thread for sending data */
	protected static class SendThread implements Runnable {
		private TcpServer server;
		private SelectionKey key;
		private TcpConnection connection;

		public SendThread(TcpServer server, SelectionKey key) {
			this.server = server;
			this.key = key;
			this.connection = (TcpConnection) key.attachment();
			this.connection.setWriting(true);
		}

		public TcpServer getServer() {
			return this.server;
		}

		@Override
		public void run() {
			try {
				handleWrite();
			} finally {
				connection.setWriting(false);
				setOPWrite(key, connection.hasSendingData());
			}
		}

		public void handleWrite() {
			System.out.println("session write key=" + key);
			
			if (!key.isValid() || !key.isWritable())
				return;

			if (!connection.hasSendingData()) 
				return;

			// convert to ByteBuffer
			ByteBuffer b = connection.getWriter().toByteBuffer();
			
			// write to the channel
			SocketChannel channel = (SocketChannel) key.channel();
			int sendBytes = 0;
			try {
				sendBytes = channel.write(b);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// process remaining bytes
			try {
				System.out.println("sent bytes " + sendBytes + ", remain bytes " + b.remaining());
				connection.getWriter().remain(b.remaining());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// if all bytes is sent, and do not keep alive
			if (b.remaining() == 0 && !connection.keepAlive()) {
				try {
					key.cancel();
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

}
