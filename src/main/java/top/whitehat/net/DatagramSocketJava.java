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

import top.whitehat.net.DatagramSocket.SocketEvent;
import top.whitehat.net.DatagramSocket.SocketEventListener;
import top.whitehat.net.DatagramSocket.SocketStatistics;

/**
 * DatagramSocketJava that wraps the standard Java DatagramSocket
 * Provides identical API to java.net.DatagramSocket
 */
public class DatagramSocketJava extends DatagramSocketDelegate {
    
    // Delegate all operations to the standard java.net.DatagramSocket
    private final java.net.DatagramSocket delegateSocket;
    
    // Track socket state
    private boolean connected = false;
    private boolean closed = false;
    private InetAddress connectedAddress = null;
    private int connectedPort = -1;
    
    // Custom configuration options (example of custom functionality)
    private boolean enableLogging = false;
    private long bytesSent = 0;
    private long bytesReceived = 0;
    private SocketEventListener eventListener = null;
    
    /**
     * Creates an unbound datagram socket
     */
    public DatagramSocketJava() throws SocketException {
        this.delegateSocket = new java.net.DatagramSocket();
        log("CustomDatagramSocket created (unbound)");
    }
    
    /**
     * Creates a datagram socket and binds it to the specified port
     */
    public DatagramSocketJava(int port) throws SocketException {
        this.delegateSocket = new java.net.DatagramSocket(port);
        log("CustomDatagramSocket created and bound to port: " + port);
    }
    
    /**
     * Creates a datagram socket and binds it to the specified local address and port
     */
    public DatagramSocketJava(int port, InetAddress laddr) throws SocketException {
        this.delegateSocket = new java.net.DatagramSocket(port, laddr);
        log("CustomDatagramSocket created and bound to " + 
            (laddr != null ? laddr.getHostAddress() : "null") + ":" + port);
    }
    
    /**
     * Creates a datagram socket with the specified SocketAddress
     */
    public DatagramSocketJava(SocketAddress bindaddr) throws SocketException {
        this.delegateSocket = new java.net.DatagramSocket(bindaddr);
        if (bindaddr instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress) bindaddr;
            log("CustomDatagramSocket created and bound to " + 
                addr.getAddress().getHostAddress() + ":" + addr.getPort());
        }
    }
    
    /**
     * Creates an unbound socket with the specified DatagramSocketImpl
     * Note: This is provided for compatibility but uses standard implementation
     * @throws SocketException 
     */
    protected DatagramSocketJava(DatagramSocketImpl impl) throws SocketException {
        // Since we can't access the protected constructor easily, we'll use a different approach
        this.delegateSocket = new java.net.DatagramSocket();
        log("CustomDatagramSocket created with custom impl (using standard socket)");
    }
    
    /**
     * Binds this socket to the specified address
     */
    public void bind(SocketAddress addr) throws SocketException {
        checkClosed();
        delegateSocket.bind(addr);
        if (addr instanceof InetSocketAddress) {
            InetSocketAddress inetAddr = (InetSocketAddress) addr;
            log("Socket bound to " + inetAddr.getAddress().getHostAddress() + ":" + inetAddr.getPort());
        }
    }
    
    /**
     * Connects this socket to a remote address
     */
    public void connect(InetAddress address, int port) {
        checkClosed();
        delegateSocket.connect(address, port);
        this.connectedAddress = address;
        this.connectedPort = port;
        this.connected = true;
        log("Socket connected to " + address.getHostAddress() + ":" + port);
        notifyEvent(SocketEvent.CONNECTED, address, port);
    }
    
    /**
     * Connects this socket to a remote socket address
     */
    public void connect(SocketAddress addr) throws SocketException {
        checkClosed();
        if (addr instanceof InetSocketAddress) {
            InetSocketAddress inetAddr = (InetSocketAddress) addr;
            connect(inetAddr.getAddress(), inetAddr.getPort());
        } else {
            throw new IllegalArgumentException("Unsupported address type");
        }
    }
    
    /**
     * Disconnects the socket
     */
    public void disconnect() {
        if (connected) {
            InetAddress oldAddress = connectedAddress;
            int oldPort = connectedPort;
            
            delegateSocket.disconnect();
            this.connected = false;
            this.connectedAddress = null;
            this.connectedPort = -1;
            
            log("Socket disconnected from " + 
                (oldAddress != null ? oldAddress.getHostAddress() : "null") + ":" + oldPort);
            notifyEvent(SocketEvent.DISCONNECTED, oldAddress, oldPort);
        }
    }
    
    /**
     * Returns the remote address this socket is connected to
     */
    public InetAddress getInetAddress() {
        return connected ? connectedAddress : delegateSocket.getInetAddress();
    }
    
    /**
     * Returns the remote port this socket is connected to
     */
    public int getPort() {
        return connected ? connectedPort : delegateSocket.getPort();
    }
    
    /**
     * Returns the remote socket address
     */
    public SocketAddress getRemoteSocketAddress() {
        if (connected && connectedAddress != null) {
            return new InetSocketAddress(connectedAddress, connectedPort);
        }
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
        checkClosed();
        
        // Custom pre-processing
        if (enableLogging) {
            log("Sending packet to " + 
                (p.getAddress() != null ? p.getAddress().getHostAddress() : "null") + 
                ":" + p.getPort() + ", length: " + p.getLength());
        }
        
        long startTime = System.nanoTime();
        delegateSocket.send(p);
        long endTime = System.nanoTime();
        
        // Update statistics
        bytesSent += p.getLength();
        
        // Custom post-processing
        if (enableLogging) {
            log("Packet sent successfully in " + (endTime - startTime) + " ns");
        }
        
        notifyEvent(SocketEvent.PACKET_SENT, p.getAddress(), p.getPort(), p.getLength());
    }
    
    /**
     * Receives a datagram packet
     */
    public synchronized void receive(DatagramPacket p) throws IOException {
        checkClosed();
        
        if (enableLogging) {
            log("Waiting to receive packet, buffer size: " + p.getLength());
        }
        
        long startTime = System.nanoTime();
        delegateSocket.receive(p);
        long endTime = System.nanoTime();
        
        // Update statistics
        bytesReceived += p.getLength();
        
        if (enableLogging) {
            log("Received packet from " + 
                (p.getAddress() != null ? p.getAddress().getHostAddress() : "null") + 
                ":" + p.getPort() + ", length: " + p.getLength() + 
                " in " + (endTime - startTime) + " ns");
        }
        
        notifyEvent(SocketEvent.PACKET_RECEIVED, p.getAddress(), p.getPort(), p.getLength());
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
        checkClosed();
        delegateSocket.setSoTimeout(timeout);
        log("Set socket timeout to: " + timeout + "ms");
    }
    
    public int getSoTimeout() throws SocketException {
        return delegateSocket.getSoTimeout();
    }
    
    public void setSendBufferSize(int size) throws SocketException {
        checkClosed();
        delegateSocket.setSendBufferSize(size);
        log("Set send buffer size to: " + size);
    }
    
    public int getSendBufferSize() throws SocketException {
        return delegateSocket.getSendBufferSize();
    }
    
    public void setReceiveBufferSize(int size) throws SocketException {
        checkClosed();
        delegateSocket.setReceiveBufferSize(size);
        log("Set receive buffer size to: " + size);
    }
    
    public int getReceiveBufferSize() throws SocketException {
        return delegateSocket.getReceiveBufferSize();
    }
    
    public void setReuseAddress(boolean on) throws SocketException {
        checkClosed();
        delegateSocket.setReuseAddress(on);
        log("Set reuse address to: " + on);
    }
    
    public boolean getReuseAddress() throws SocketException {
        return delegateSocket.getReuseAddress();
    }
    
    public void setBroadcast(boolean on) throws SocketException {
        checkClosed();
        delegateSocket.setBroadcast(on);
        log("Set broadcast to: " + on);
    }
    
    public boolean getBroadcast() throws SocketException {
        return delegateSocket.getBroadcast();
    }
    
    public void setTrafficClass(int tc) throws SocketException {
        checkClosed();
        delegateSocket.setTrafficClass(tc);
        log("Set traffic class to: " + tc);
    }
    
    public int getTrafficClass() throws SocketException {
        return delegateSocket.getTrafficClass();
    }
    
    /**
     * Socket state queries
     */
    
    public boolean isConnected() {
        return connected || delegateSocket.isConnected();
    }
    
    public boolean isBound() {
        return delegateSocket.isBound();
    }
    
    public boolean isClosed() {
        return closed || delegateSocket.isClosed();
    }
    
    /**
     * Closes this socket
     */
    public void close() {
        if (!closed) {
            closed = true;
            delegateSocket.close();
            log("Socket closed");
            notifyEvent(SocketEvent.CLOSED, null, -1);
        }
    }
    
    /**
     * Custom functionality: Enable/disable logging
     */
    public void setLoggingEnabled(boolean enabled) {
        this.enableLogging = enabled;
    }
    
    public boolean isLoggingEnabled() {
        return enableLogging;
    }
    
    /**
     * Custom functionality: Get statistics
     */
    public SocketStatistics getStatistics() {
        return new SocketStatistics(bytesSent, bytesReceived);
    }
    
    /**
     * Custom functionality: Reset statistics
     */
    public void resetStatistics() {
        bytesSent = 0;
        bytesReceived = 0;
        log("Statistics reset");
    }
    
    /**
     * Custom functionality: Set event listener
     */
    public void setEventListener(SocketEventListener listener) {
        this.eventListener = listener;
    }
    
    /**
     * Check if socket is closed
     */
    private void checkClosed() {
        if (closed || delegateSocket.isClosed()) {
            throw new IllegalStateException("Socket is closed");
        }
    }
    
    /**
     * Internal logging method
     */
    private void log(String message) {
        if (enableLogging) {
            System.out.println("[CustomDatagramSocket] " + message);
        }
    }
    
    /**
     * Notify event listener
     */
    private void notifyEvent(SocketEvent event, InetAddress address, int port) {
        notifyEvent(event, address, port, 0);
    }
    
    private void notifyEvent(SocketEvent event, InetAddress address, int port, int dataLength) {
        if (eventListener != null) {
            try {
                eventListener.onSocketEvent(event, address, port, dataLength);
            } catch (Exception e) {
                // Don't let listener exceptions break socket operations
                System.err.println("Error in socket event listener: " + e.getMessage());
            }
        }
    }
      
}
