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
package top.whitehat.net;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Map;

import top.whitehat.net.DatagramSocket.SocketEventListener;
import top.whitehat.net.DatagramSocket.SocketStatistics;

/**
 * DelegateDatagramSocket provides identical API to java.net.DatagramSocket
 */
public abstract class DatagramSocketDelegate implements  java.io.Closeable {

	public static Map<String, Class<? extends DatagramSocketDelegate>> classMap = //
			new HashMap<String, Class<? extends DatagramSocketDelegate>>() {
				private static final long serialVersionUID = 1L;
				{
					put("java", DatagramSocketJava.class);
				}
			};
			
	public static Class<? extends DatagramSocketDelegate> findClass(String name) {
		return classMap.getOrDefault(name, null);
	}

	public static String classType = "java";

	public static DatagramSocketDelegate newInstance() {
		Class<? extends DatagramSocketDelegate> cls = findClass(classType);
		try {
			return cls.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public static DatagramSocketDelegate newInstance(int port) {
		Class<? extends DatagramSocketDelegate> cls = findClass(classType);
		try {
			return cls.getConstructor(int.class).newInstance(port);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public static DatagramSocketDelegate newInstance(int port, InetAddress laddr) {
		Class<? extends DatagramSocketDelegate> cls = findClass(classType);
		try {
			return cls.getConstructor(int.class, InetAddress.class).newInstance(port, laddr);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public static DatagramSocketDelegate newInstance(SocketAddress bindaddr) {
		Class<? extends DatagramSocketDelegate> cls = findClass(classType);
		try {
			return cls.getConstructor(SocketAddress.class).newInstance(bindaddr);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public static DatagramSocketDelegate newInstance(DatagramSocketImpl impl) {
		Class<? extends DatagramSocketDelegate> cls = findClass(classType);
		try {
			return cls.getConstructor(DatagramSocketImpl.class).newInstance(impl);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

//	/**
//	 * Creates an unbound datagram socket
//	 */
//	public DatagramSocketDelegate() throws SocketException {
//	}
//
//	/**
//	 * Creates a datagram socket and binds it to the specified port
//	 */
//	public DatagramSocketDelegate(int port) throws SocketException {
//	}
//
//	/**
//	 * Creates a datagram socket and binds it to the specified local address and
//	 * port
//	 */
//	public DatagramSocketDelegate(int port, InetAddress laddr) throws SocketException {
//	}
//
//	/**
//	 * Creates a datagram socket with the specified SocketAddress
//	 */
//	public DatagramSocketDelegate(SocketAddress bindaddr) throws SocketException {
//	}
//
//	/**
//	 * Creates an unbound socket with the specified DatagramSocketImpl Note: This is
//	 * provided for compatibility but uses standard implementation
//	 * 
//	 * @throws SocketException
//	 */
//	protected DatagramSocketDelegate(DatagramSocketImpl impl) throws SocketException {
//
//	}

	/**
	 * Binds this socket to the specified address
	 */
	public abstract void bind(SocketAddress addr) throws SocketException;

	/**
	 * Connects this socket to a remote address
	 */
	public abstract void connect(InetAddress address, int port);

	/**
	 * Connects this socket to a remote socket address
	 */
	public abstract void connect(SocketAddress addr) throws SocketException;

	/**
	 * Disconnects the socket
	 */
	public abstract void disconnect();

	/**
	 * Returns the remote address this socket is connected to
	 */
	public abstract InetAddress getInetAddress();

	/**
	 * Returns the remote port this socket is connected to
	 */
	public abstract int getPort();

	/**
	 * Returns the remote socket address
	 */
	public abstract SocketAddress getRemoteSocketAddress();

	/**
	 * Returns the local socket address
	 */
	public abstract SocketAddress getLocalSocketAddress();

	/**
	 * Returns the local address
	 */
	public abstract InetAddress getLocalAddress();

	/**
	 * Returns the local port
	 */
	public abstract int getLocalPort();

	/**
	 * Sends a datagram packet
	 */
	public abstract void send(DatagramPacket p) throws IOException;

	/**
	 * Receives a datagram packet
	 */
	public synchronized void receive(DatagramPacket p) throws IOException {

	}

	/**
	 * Returns the DatagramChannel associated with this socket
	 */
	public abstract DatagramChannel getChannel();

	/**
	 * Socket options configuration
	 */

	public abstract void setSoTimeout(int timeout) throws SocketException;

	public abstract int getSoTimeout() throws SocketException;

	public abstract void setSendBufferSize(int size) throws SocketException;

	public abstract int getSendBufferSize() throws SocketException;

	public abstract void setReceiveBufferSize(int size) throws SocketException;

	public abstract int getReceiveBufferSize() throws SocketException;

	public abstract void setReuseAddress(boolean on) throws SocketException;

	public abstract boolean getReuseAddress() throws SocketException;

	public abstract void setBroadcast(boolean on) throws SocketException;

	public abstract boolean getBroadcast() throws SocketException;

	public abstract void setTrafficClass(int tc) throws SocketException;

	public abstract int getTrafficClass() throws SocketException;

	/**
	 * Socket state queries
	 */
	public abstract boolean isConnected();

	public abstract boolean isBound();

	public abstract boolean isClosed();

	/**
	 * Closes this socket
	 */
	public abstract void close();

	/**
	 * Custom functionality: Enable/disable logging
	 */
	public abstract void setLoggingEnabled(boolean enabled);

	public abstract boolean isLoggingEnabled();

	/**
	 * Custom functionality: Get statistics
	 */
	public abstract SocketStatistics getStatistics();

	/**
	 * Custom functionality: Reset statistics
	 */
	public abstract void resetStatistics();

	/**
	 * Custom functionality: Set event listener
	 */
	public abstract void setEventListener(SocketEventListener listener);

	/**
	 * Finalizer to ensure socket is closed
	 */
//    protected abstract  void finalize() throws Throwable;

}
