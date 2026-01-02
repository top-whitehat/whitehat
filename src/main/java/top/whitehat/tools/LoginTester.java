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
package top.whitehat.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import top.whitehat.client.ILogin;

/**
 * Try to login with a dictionary.
 * 
 * This tool is intended strictly for authorized security testing​ and
 * self-assessment. 
 * Do not use it against systems or accounts without explicit,
 * written permission.
 */
public class LoginTester {

	public interface ProgressHandler {
		public boolean onProgress(int count);
	}

	private Class<? extends ILogin> loginClass;
	private String host;
	private String username;
	private int threads = 8;	
	private int total = 0;
	private int progressStep = 10;
	private String finalPassword = null;

	private AtomicBoolean found = new AtomicBoolean(false);
	private AtomicInteger attempts = new AtomicInteger(0);
	private ProgressHandler handler = null;

	/** Constructor */
	public LoginTester(Class<? extends ILogin> loginClass, String host) throws InstantiationException {
		try {
			this.loginClass = loginClass;
			this.loginClass.getConstructor().newInstance();
			this.host = host;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new InstantiationException(e.getClass().getSimpleName() + " " + e.getMessage());
		}
	}

	/** get threads count */
	public int getThreads() {
		return threads;
	}

	/** set threads count */
	public LoginTester setThreads(int value) {
		threads = value;
		return this;
	}
	
	/** get progress step: how many passwords tested when progress event is triggered */
	public int getProgressStep() {
		return progressStep;
	}

	/** set progress step: how many passwords tested when progress event is triggered */
	public LoginTester setProgressStep(int value) {
		progressStep = value;
		return this;
	}

	/** Try to login with password from the passwords list.
	 *  return the valid password.
	 *  
	 * @param username    The user name to login
	 * @param passwords   The passwords dictionary 
	 * 
	 * @return  return the valid password.
	 */
	public String tryPasswords(String username, List<String> passwords) {
		this.username = username;
		finalPassword = null;
		
		BlockingQueue<String> queue = new LinkedBlockingQueue<>(passwords);
		ExecutorService exec = Executors.newFixedThreadPool(getThreads());

		// create multiple threads
		total = passwords.size();
		if (total == 0) return null;
		for (int i = 0; i < Math.min(getThreads(), total); i++) {
			exec.submit(new Worker(queue));
		}

		// Wait until found or all tasks complete
		try {
			while (!found.get() && !exec.awaitTermination(500, TimeUnit.MILLISECONDS)) {
				// Periodically check if we should stop
			}
		} catch (InterruptedException e) {

		} finally {
			exec.shutdownNow();
		}

		// return what we found
		return finalPassword;

	}

	/** Cancel operations */
	public void cancel() {
		found.set(true);
	}

	/** set progress handler */
	public LoginTester onProgress(ProgressHandler h) {
		this.handler = h;
		return this;
	}

	/**
	 * Worker task: consumes passwords from the queue and tries them until found or
	 * interrupted.
	 */
	class Worker implements Runnable {
		private final BlockingQueue<String> queue;

		Worker(BlockingQueue<String> queue) {
			this.queue = queue;
		}

		@Override
		public void run() {
			ILogin loginObj = null;
			try {
				// Stops all workers as soon as a valid password is found.
				while (!found.get() && !Thread.currentThread().isInterrupted()) {
					String pwd = queue.poll(100, TimeUnit.MILLISECONDS);
					if (pwd == null) {
						found.set(true);
					}

					// trigger onProgress event
					int count = attempts.incrementAndGet();
					if (count % progressStep == 0 && handler != null) {
						if (false == handler.onProgress(count))
							cancel();
					}
					
					try {
						// create a ILogin object
						loginObj = loginClass.getConstructor().newInstance();
						loginObj.setHost(host);
	
						// try login
						if (loginObj.login(username, pwd)) {
							finalPassword = pwd;
							found.set(true);
							break;
						}
					} catch (Exception e) {
						// loop
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				
			} finally {
				if (loginObj != null)
					loginObj.logout();
			}
		}
	}

}
