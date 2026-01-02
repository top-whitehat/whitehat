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
import java.net.ServerSocket;
import java.net.Socket;


/** TcpService that using ServerSocket */
public class TcpSocketService extends TcpService {
	
	private TcpServer server;
	
	private ServerSocket serverSocket;
	
	/** Constructor */
	public TcpSocketService() {
		super();
	}
	
	/** Initialization */
	public TcpSocketService init(TcpServer server) throws IOException {
		this.server = server;
		serverSocket = new ServerSocket(server.getPort());
		return this;
	}
	
	/** Start service */
	public void start() {
		while (server.isRunning()) {
            try {
            	Socket clientSocket = serverSocket.accept();
				server.execute(new TcpSession(server, clientSocket));
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
	
	/** Close service */
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			serverSocket = null;
		}
	}
	
	public static class TcpSession implements Runnable {
		TcpServer server;
		Socket clientSocket;
		TcpConnection connection;
		
		public TcpSession(TcpServer server, Socket clientSocket) throws IOException {
			this.server = server;
			this.clientSocket = clientSocket;
			this.connection = new TcpConnection(server, getTcpKey());
			
			InputStream inStream = clientSocket.getInputStream();
			OutputStream outStream = clientSocket.getOutputStream();
			connection.setStreams(inStream, outStream);
		}
		
		private TcpKey getTcpKey() {
			TcpKey key = new TcpKey();
			key.srcIp = clientSocket.getInetAddress();
			key.srcPort = clientSocket.getPort();
			key.dstIp = clientSocket.getLocalAddress();
			key.dstPort = clientSocket.getLocalPort();
			return key;
		}

		@Override
		public void run() {
			// process request data
			server.dataIncome(connection);
			
			// if do not keep alive
			if (!connection.keepAlive()) {
				try {
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	
}
