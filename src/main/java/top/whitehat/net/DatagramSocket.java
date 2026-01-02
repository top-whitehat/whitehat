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
import java.net.*;
import java.nio.channels.DatagramChannel;

/**
 * DatagramSocket that wraps the standard Java DatagramSocket
 * Provides identical API to java.net.DatagramSocket but with custom internal implementation
 */
public class DatagramSocket implements java.io.Closeable {
    
    // Delegate all operations to the standard java.net.DatagramSocket
    private final DatagramSocketDelegate delegateSocket;

    /**
     * Creates an unbound datagram socket
     */
    public DatagramSocket() throws SocketException {
        this.delegateSocket = DatagramSocketDelegate.newInstance();
    }
    
    /**
     * Creates a datagram socket and binds it to the specified port
     */
    public DatagramSocket(int port) throws SocketException {
        this.delegateSocket = DatagramSocketDelegate.newInstance(port);
    }
    
    /**
     * Creates a datagram socket and binds it to the specified local address and port
     */
    public DatagramSocket(int port, InetAddress laddr) throws SocketException {
        this.delegateSocket = DatagramSocketDelegate.newInstance(port, laddr);
    }
    
    /**
     * Creates a datagram socket with the specified SocketAddress
     */
    public DatagramSocket(SocketAddress bindaddr) throws SocketException {
        this.delegateSocket = DatagramSocketDelegate.newInstance(bindaddr);
    }
    
    /**
     * Creates an unbound socket with the specified DatagramSocketImpl
     * Note: This is provided for compatibility but uses standard implementation
     * @throws SocketException 
     */
    protected DatagramSocket(DatagramSocketImpl impl) throws SocketException {
        this.delegateSocket = DatagramSocketDelegate.newInstance();
    }
    
    /**
     * Binds this socket to the specified address
     */
    public void bind(SocketAddress addr) throws SocketException {
        delegateSocket.bind(addr);
    }
    
    /**
     * Connects this socket to a remote address
     */
    public void connect(InetAddress address, int port) {
        delegateSocket.connect(address, port);
    }
    
    /**
     * Connects this socket to a remote socket address
     */
    public void connect(SocketAddress addr) throws SocketException {
    	delegateSocket.connect(addr);
    }
    
    /**
     * Disconnects the socket
     */
    public void disconnect() {
    	delegateSocket.disconnect();
    }
    
    /**
     * Returns the remote address this socket is connected to
     */
    public InetAddress getInetAddress() {
    	return delegateSocket.getInetAddress();
    }
    
    /**
     * Returns the remote port this socket is connected to
     */
    public int getPort() {
    	return delegateSocket.getPort();
    }
    
    /**
     * Returns the remote socket address
     */
    public SocketAddress getRemoteSocketAddress() {
        return delegateSocket.getRemoteSocketAddress();
    }
    
    /**
     * Returns the local socket address
     */
    public SocketAddress getLocalSocketAddress() {
        return delegateSocket.getLocalSocketAddress();
    }
    
    /**
     * Returns the local address
     */
    public InetAddress getLocalAddress() {
        return delegateSocket.getLocalAddress();
    }
    
    /**
     * Returns the local port
     */
    public int getLocalPort() {
        return delegateSocket.getLocalPort();
    }
    
    /**
     * Sends a datagram packet
     */
    public void send(DatagramPacket p) throws IOException {
    	delegateSocket.send(p);
    }
    
    /**
     * Receives a datagram packet
     */
    public synchronized void receive(DatagramPacket p) throws IOException {
    	delegateSocket.receive(p);
    }
    
    /**
     * Returns the DatagramChannel associated with this socket
     */
    public DatagramChannel getChannel() {
        return delegateSocket.getChannel();
    }
    
    /**
     * Socket options configuration
     */
    
    public void setSoTimeout(int timeout) throws SocketException {
        delegateSocket.setSoTimeout(timeout);
    }
    
    public int getSoTimeout() throws SocketException {
        return delegateSocket.getSoTimeout();
    }
    
    public void setSendBufferSize(int size) throws SocketException {
        delegateSocket.setSendBufferSize(size);
    }
    
    public int getSendBufferSize() throws SocketException {
        return delegateSocket.getSendBufferSize();
    }
    
    public void setReceiveBufferSize(int size) throws SocketException {
        delegateSocket.setReceiveBufferSize(size);
    }
    
    public int getReceiveBufferSize() throws SocketException {
        return delegateSocket.getReceiveBufferSize();
    }
    
    public void setReuseAddress(boolean on) throws SocketException {
        delegateSocket.setReuseAddress(on);
    }
    
    public boolean getReuseAddress() throws SocketException {
        return delegateSocket.getReuseAddress();
    }
    
    public void setBroadcast(boolean on) throws SocketException {
        delegateSocket.setBroadcast(on);
    }
    
    public boolean getBroadcast() throws SocketException {
        return delegateSocket.getBroadcast();
    }
    
    public void setTrafficClass(int tc) throws SocketException {
        delegateSocket.setTrafficClass(tc);
    }
    
    public int getTrafficClass() throws SocketException {
        return delegateSocket.getTrafficClass();
    }
    
    /**
     * Socket state queries
     */
    
    public boolean isConnected() {
        return delegateSocket.isConnected();
    }
    
    public boolean isBound() {
        return delegateSocket.isBound();
    }
    
    public boolean isClosed() {
        return delegateSocket.isClosed();
    }
    
    /**
     * Closes this socket
     */
    public void close() {
    	delegateSocket.close();
    }
    
    /**
     * Custom functionality: Enable/disable logging
     */
    public void setLoggingEnabled(boolean enabled) {
    	delegateSocket.setLoggingEnabled(enabled);
    }
    
    public boolean isLoggingEnabled() {
        return delegateSocket.isLoggingEnabled();
    }
    
    /**
     * Custom functionality: Get statistics
     */
    public SocketStatistics getStatistics() {
    	return delegateSocket.getStatistics();
    }
    
    /**
     * Custom functionality: Reset statistics
     */
    public void resetStatistics() {
    	delegateSocket.resetStatistics();
    }
    
    /**
     * Custom functionality: Set event listener
     */
    public void setEventListener(SocketEventListener listener) {
    	delegateSocket.setEventListener(listener);
    }

    /**
     * Custom event types
     */
    public enum SocketEvent {
        CONNECTED,
        DISCONNECTED,
        PACKET_SENT,
        PACKET_RECEIVED,
        CLOSED
    }
    
    /**
     * Socket event listener interface
     */
    public interface SocketEventListener {
        void onSocketEvent(SocketEvent event, InetAddress address, int port, int dataLength);
    }
    
    /**
     * Socket statistics container
     */
    public static class SocketStatistics {
        private final long bytesSent;
        private final long bytesReceived;
        private final long timestamp;
        
        public SocketStatistics(long bytesSent, long bytesReceived) {
            this.bytesSent = bytesSent;
            this.bytesReceived = bytesReceived;
            this.timestamp = System.currentTimeMillis();
        }
        
        public long getBytesSent() { return bytesSent; }
        public long getBytesReceived() { return bytesReceived; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("SocketStatistics{bytesSent=%d, bytesReceived=%d, timestamp=%d}",
                bytesSent, bytesReceived, timestamp);
        }
    }
}
