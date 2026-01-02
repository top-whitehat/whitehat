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

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import top.whitehat.NetCard;
import top.whitehat.NetEventListener;
import top.whitehat.packet.Packet;


/** A server that use thread poll to  process packets */
public abstract class PacketServer {

	/** thread pool executor */
	protected ThreadPoolExecutor threadPoolExecutor = null;
	
	/** Indicates whether the server is running **/
	protected boolean running = false;
	
	/** the number of threads to keep in the pool */
	protected int corePoolSize = 8;
	
	/**  the maximum number of threads to allow in the pool */
	protected int maximumPoolSize = 16;
	
	/** when the number of threads is greater than the core, this is 
	 * the maximum time that excess idle threads will wait for 
	 */
	protected int keepAliveTime = 5;
	
	/** size of queue */
	protected int queueSize = 5000;
	
	/** Indicates whether the thread pool's params is set */
	protected boolean threadPoolSpecified = false;
	
	/** server port */
	private int port;
	
	/** the network card object */
	protected NetCard netCard;

	/** Constructor */
	public PacketServer(NetCard netCard) {
		super();
		bindNetCard(netCard);
	}
	
	/** bind netCard.onPacket(NetCard netCard) */
	protected void bindNetCard(NetCard netCard) {
		this.netCard = netCard;
		
		// when network card has income packet
		this.netCard.onPacket(packet-> {
			if (filter(packet)) {
				// add receive task to thread pool
				threadPoolExecutor.execute(new ReceiveThread(this, packet));
			}
		});
	}
	
	/** get port */
	public int getPort() {
		return port;
	}
	
	/** set port */
	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * set parameters of thread pool
	 * 
	 * @param corePoolSize     the number of threads to keep in the pool
	 * @param maximumPoolSize   the maximum number of threads to allow in the pool
	 * @param keepAliveTime     when the number of threads is greater than the core, 
	 * 		this is the maximum time that excess idle threads will wait for 
	 * 	    new tasks before terminating.
	 * @param queueSize      
	 */
	public void setThreadPool(int corePoolSize, int maximumPoolSize, int keepAliveTime, int queueSize) {
		this.corePoolSize = corePoolSize;
		this.maximumPoolSize = maximumPoolSize;
		this.keepAliveTime = keepAliveTime;
		this.queueSize = queueSize;
		threadPoolSpecified = true;
	}
	
	/** automatically set thread pool params by CPU cores and memory
	 *  subclass should override this method to set parameters of thread pool.
	 */
	protected void autoThreadPoolSettings() {
		// thread count is depend on by CPU cores
		int coreCount = Runtime.getRuntime().availableProcessors();
		corePoolSize = coreCount + 1;
		maximumPoolSize = 4 * coreCount;
		
		// queue size is depend on memory size
		long maxMemoryBytes = Runtime.getRuntime().maxMemory();
		long count = (long)((maxMemoryBytes * 0.8) / 512);
		int size = (int) (count > Integer.MAX_VALUE ? Integer.MAX_VALUE : count);
		queueSize = size;
	}
	
	/** create ThreadPool  */
	protected void createThreadPool() {
		if (!threadPoolSpecified) autoThreadPoolSettings();			 
		
		if (threadPoolExecutor == null) {
			LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(queueSize);
			
			threadPoolExecutor = new ThreadPoolExecutor( //
					this.corePoolSize, // the number of threads to keep in the pool
					this.maximumPoolSize, //  the maximum number of threads to allow in thepool
					this.keepAliveTime, // 
					TimeUnit.SECONDS, // time unit
					queue, // the queue to use for holding tasks before they are executed. 
					Executors.defaultThreadFactory(), //
					new ThreadPoolExecutor.DiscardOldestPolicy() ///
			);
		}
	}
	
	/** close ThreadPool */
	protected void closeThreadPool() {
		threadPoolExecutor.shutdown(); // stop add task to thread pool
	}
	
	/** Indicates whether the server is running */
	public boolean isRunning() {
		return running;
	}
	
	/** set the server's running mode */
	protected void setRunning(boolean value) {
		running = value;
	}
	
	/**
	 * Define an 'onStart' event listener that is triggered when packet capture
	 * starts.​
	 */
	public PacketServer onStart(NetEventListener listener) {
		netCard.onStart(listener);
		return this;
	}
	
	/**
	 * Define an 'onStop' event listener that is triggered when packet capture
	 * starts.​
	 */
	public PacketServer onStop(NetEventListener listener) {
		netCard.onStop(listener);
		return this;
	}
	
	/** start server */
	public void start() {
		createThreadPool();
		setRunning(true);
		netCard.setPort(getPort());
		netCard.start();
	}

	/** stop server */
	public void stop() {
		setRunning(false);
		netCard.stop();
		netCard.setPort(0);
		closeThreadPool();
	}
	
	/** return false if the packet should discard */
	public boolean filter(Packet packet) {
		return true;
	}

	/** called when packet incomes */
	protected abstract void receivePacket(Packet packet);
	
	/** send the packet */
	protected void sendPacket(Packet packet) {
		// add task to thread pool
		threadPoolExecutor.execute(new SendThread(this, packet));
	}
	
	public static class ReceiveThread implements Runnable {
		PacketServer server;
		Packet packet;
		
		public ReceiveThread(PacketServer server, Packet packet) {
			this.server = server;
			this.packet = packet;
		}

		@Override
		public void run() {
			server.receivePacket(packet);
		}

	}
	
	public static class SendThread implements Runnable {
		PacketServer server;
		Packet packet;
		
		public SendThread(PacketServer server, Packet packet) {
			this.server = server;
			this.packet = packet;
		}

		@Override
		public void run() {
			server.netCard.sendPacket(packet);
		}

	}
	
}
