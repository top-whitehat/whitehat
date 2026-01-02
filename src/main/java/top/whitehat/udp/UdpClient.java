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
package top.whitehat.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

import top.whitehat.net.DatagramSocket;

public class UdpClient {
	
	/**  Make UDP request to specified host and port, and get response
	 * 
	 * @param host    The destination address
	 * @param port    The destination host
	 * @param data    The request data
	 * @param timeout  timeout in milliseconds
	 * @return return response data bytes if success, return null if failed.
	 * @throws IOException
	 */
	public static byte[] request(InetAddress host, int port, byte[] data, int timeout) throws IOException {
		return new UdpClient().getResponse(host, port, data, timeout);
	}
	
	/** Make UDP request and get response
	 * 
	 * @param host    The destination address
	 * @param port    The destination host
	 * @param data   The request data
	 * @return return response data bytes if success, return null if failed.
	 * @throws IOException
	 */
	public static byte[] request(InetAddress host, int port, byte[] data) throws IOException {
		return request(host, port, data, 2000);
	}
	
	
	/** The fixed port for the client  */
	private int srcPort = 0;
	
	/** the internal DatagramSocket */
	private DatagramSocket innerSocket;
	
	/** buffer size for a UDP packet */
	private int bufferSize = 4096;
	
	/** Constructor */
	public UdpClient() {
		this(0);
	}
	
	/** Constructor: create a UDP client with fixed port */ 
	public UdpClient(int port) {
		this.srcPort = port;
		if (srcPort > 0) {
			try {
				innerSocket = new DatagramSocket(srcPort);
			} catch (SocketException e) {
				String msg = "cannot create DatagramSocket on port " + srcPort;
				throw new RuntimeException(msg + ", " + e.getMessage());
			}
		}
	}
	
	/** Send data to specified host and port
	 * 
	 * @param host   The destination address
	 * @param port   The destination port
	 * @param data   The data to sent
	 * @return DatagramSocket
	 * @throws IOException
	 */
	protected DatagramSocket send(InetAddress host, int port, byte[] data) throws IOException {
		DatagramSocket socket = innerSocket != null ? innerSocket : new DatagramSocket();
		socket.connect(host, port);
		
		DatagramPacket pkt = new DatagramPacket(data, data.length, host, port);
		socket.send(pkt);
		return socket;
	}
	
	/** Send data to specified host and port, and wait for response until timeout
	 * 
	 * @param host     The server address
	 * @param port     The server port
	 * @param data     The request data
	 * @param timeout  Waiting timeout in milliseconds
	 * @return DatagramPacket
	 * @throws IOException
	 */
	protected DatagramPacket sendAndWait(InetAddress host, int port, byte[] data, int timeout) throws IOException {
		DatagramSocket socket = send(host, port, data);
		DatagramPacket receivePkt = new DatagramPacket(new byte[bufferSize], bufferSize);
		if (timeout >= 0) socket.setSoTimeout(timeout);
		socket.receive(receivePkt);
		return receivePkt;
	}
	
	/** Send data to specified host and port, and wait for response until timeout
	 * 
	 * @param host     The server address
	 * @param port     The server port
	 * @param data     The request data
	 * @param timeout  Waiting timeout in milliseconds
	 * @return DatagramPacket
	 * @throws IOException
	 */
	protected byte[] getData(DatagramPacket packet) throws IOException {
		byte[] ret = new byte[packet.getLength()];
		System.arraycopy(packet.getData(), 0, ret, 0, packet.getLength());
		return ret;
	}
	
	
	/** Send data to specified host and port, and wait for response until timeout
	 * 
	 * @param host     The server address
	 * @param port     The server port
	 * @param data     The request data
	 * @param timeout  Waiting timeout in milliseconds
	 * @return return response data bytes if success, return null if failed.
	 * @throws IOException
	 */
	public byte[] getResponse(InetAddress host, int port, byte[] request, int timeout) throws IOException {
		DatagramPacket pkt = sendAndWait(host, port, request, timeout);
		return getData(pkt);
	}

}
