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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** A TCP server */
public class TcpServer {

	/** Thread pool executor */
	protected ThreadPoolExecutor threadPoolExecutor = null;

	/** Indicates whether the server is running **/
	protected boolean running = false;

	/** The number of threads to keep in the pool */
	protected int corePoolSize = 8;

	/** The maximum number of threads to allow in the pool */
	protected int maximumPoolSize = 16;

	/**
	 * When the number of threads is greater than the core, this is the maximum time
	 * that excess idle threads will wait for
	 */
	protected int keepAliveTime = 5;

	/** Size of task queue in thread pool */
	protected int queueSize = 5000;

	/** Indicates whether the thread pool's parameters is set */
	protected boolean threadPoolSpecified = false;

	/** The port */
	private int port;

	/** onStart event listeners */
	private List<TcpListener> startListeners = new ArrayList<>();

	/** onConnect event listener */
	private List<TcpListener> connectListeners = new ArrayList<>();

	/** TcpService object */
	private TcpService service;
	
	/** TcpService Kind */
	private String serviceKind = "nio";

	/** Constructor */
	public TcpServer(int port) {
		super();
		this.port = port;
	}

	/** Get port */
	public int getPort() {
		return port;
	}

	/** Set port */
	public void setPort(int port) {
		if (service != null)
			throw new RuntimeException("cannot change port after service start");
		this.port = port;
	}

	/**
	 * Set parameters of thread pool
	 * 
	 * @param corePoolSize    the number of threads to keep in the pool
	 * @param maximumPoolSize the maximum number of threads to allow in the pool
	 * @param keepAliveTime   when the number of threads is greater than the core,
	 *                        this is the maximum time that excess idle threads will
	 *                        wait for new tasks before terminating.
	 * @param queueSize
	 */
	public void setThreadPool(int corePoolSize, int maximumPoolSize, int keepAliveTime, int queueSize) {
		this.corePoolSize = corePoolSize;
		this.maximumPoolSize = maximumPoolSize;
		this.keepAliveTime = keepAliveTime;
		this.queueSize = queueSize;
		threadPoolSpecified = true;
	}

	/** Subclass should override this method to set parameters of thread pool */
	protected void autoThreadPoolSettings() {

	}

	/** Create ThreadPool */
	protected void createThreadPool() {
		if (!threadPoolSpecified)
			autoThreadPoolSettings();

		if (threadPoolExecutor == null) {
			LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(queueSize);

			threadPoolExecutor = new ThreadPoolExecutor( //
					this.corePoolSize, // the number of threads to keep in the pool
					this.maximumPoolSize, // the maximum number of threads to allow in thepool
					this.keepAliveTime, //
					TimeUnit.SECONDS, // time unit
					queue, // the queue to use for holding tasks before they are executed.
					Executors.defaultThreadFactory(), //
					new ThreadPoolExecutor.DiscardOldestPolicy() ///
			);
		}
	}

	/** Close ThreadPool */
	protected void closeThreadPool() {
		// close thread pool
		if (threadPoolExecutor != null) {
			threadPoolExecutor.shutdown(); // stop add task to thread pool
			try {
				if (!threadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
					threadPoolExecutor.shutdownNow();
				}
			} catch (InterruptedException e) {
				threadPoolExecutor.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
	}

	/** Indicates whether the server is running */
	public boolean isRunning() {
		return running;
	}

	/** Set the server's running mode */
	protected void setRunning(boolean value) {
		running = value;
	}
	
	public void serviceType(String value) {
		this.serviceKind = value;
	}

	/** Start server */
	protected void startServer() throws IOException {
		if (service != null)
			closeServer();
		service = TcpService.of(serviceKind, this);
	}

	/** Close server */
	protected void closeServer() throws IOException {
		service.close();
		service = null;
	}

	/**
	 * Define an 'onStart' event listener that is triggered when server starts.​
	 */
	public TcpServer onStart(TcpListener listener) {
		startListeners.add(listener);
		return this;
	}

	/**
	 * Define an 'onConnect' event listener that is triggered when the client is
	 * connected
	 */
	public TcpServer onConnect(TcpListener listener) {
		connectListeners.add(listener);
		return this;
	}

	/** Trigger onStart event */
	protected void triggerOnStart() {
		for (TcpListener listener : startListeners)
			try {
				listener.onEvent(null);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/** Trigger onConnect event */
	protected void triggerOnConnect(TcpConnection connection) {
		for (TcpListener listener : connectListeners)
			try {
				listener.onEvent(connection);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/** Start server */
	public void start() {
		createThreadPool();
		setRunning(true);
		try {
			startServer();
			triggerOnStart();
			service.start();
		} catch (IOException e) {
			setRunning(false);
			throw new RuntimeException(e.getMessage());
		}
	}

	/** Stop server */
	public void stop() {
		try {
			closeServer();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			closeThreadPool();
			setRunning(false);
		}
	}

	/** Called when data income */
	protected void dataIncome(TcpConnection connection) {
		try {
			service(connection);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Provide service */
	protected void service(TcpConnection session) throws IOException {
	}

	/** Submit a runnable task to thread pool */
	public void execute(Runnable r) {
		threadPoolExecutor.execute(r);
	}

}
