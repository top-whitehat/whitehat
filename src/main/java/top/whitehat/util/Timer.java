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
package top.whitehat.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Timer functions
 */
public class Timer {

	/** store of task id */
	public static class TaskIds {
		private static TaskIds INSTANCE = null;

		/** Get an instance */
		public static TaskIds getInstance() {
			if (INSTANCE == null)
				INSTANCE = new TaskIds();
			return INSTANCE;
		}

		private static final Random RANDOM = new Random();

		/** Create a random integer */
		private static int randomInt() {
			int maxValue = 2_0000_0000;
			int minValue = 1000_0000;
			return RANDOM.nextInt(maxValue + 1) + minValue;
		}

		/** Create a task id */
		public static int newId() {
			return getInstance().newId(null);
		}

		/** Get executor object that bind to specified id */
		public static ScheduledExecutorService get(int id) {
			return getInstance().getId(id);
		}

		/** Bind a executor object to specified id */
		public static void put(int id, ScheduledExecutorService future) {
			getInstance().putId(id, future);
		}

		/** Check whether specified id exists */
		public static boolean exists(int id) {
			return getInstance().containsId(id);
		}

		/** Remove specified id */
		public static boolean remove(int id) {
			return getInstance().removeId(id);
		}

		// ----------- members of the instance -----------------

		/** store of id */
		private HashMap<Integer, ScheduledExecutorService> idMap = new HashMap<Integer, ScheduledExecutorService>();

		/** Create a task id and bind it to specified future object */
		public int newId(ScheduledExecutorService executor) {
			// create a task id
			int id = randomInt();
			synchronized (idMap) {
				// create a unique id
				while (idMap.containsKey(id)) {
					id = randomInt();
				}
				// store the id to task id store
				idMap.put(id, executor);
			}
			return id;
		}

		/** Get executor that bind to specified id */
		public ScheduledExecutorService getId(int id) {
			return idMap.getOrDefault(id, null);
		}

		/** Bind a executor to specified id */
		public void putId(int id, ScheduledExecutorService executor) {
			idMap.put(id, executor);
		}

		/** Check whether specified id exists */
		public boolean containsId(int id) {
			return idMap.containsKey(id);
		}

		/** Remove specified id */
		public boolean removeId(int id) {
			synchronized (idMap) {
				if (exists(id)) {
					idMap.remove(id);
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Execute a function periodically . <br>
	 * 
	 * Submits a periodic action that becomes enabled first after the given initial
	 * delay, and subsequently with the given period
	 * 
	 * 
	 * @param task         the runnable task
	 * @param initialDelay the time to delay first execution
	 * @param period       the period between successive executions (in
	 *                     milliseconds)
	 * @return return an id which can used in clearTimeout()
	 */
	public static int run(Runnable task, long initialDelay, long period) {

		// create an id
		final int id = TaskIds.newId();

		// create a executor
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

		// create a runnable
		Runnable r = () -> {
			try {
				if (TaskIds.exists(id)) {
					task.run();
				} else {
					executor.shutdown();
				}
			} catch (Exception e) {
				throw new RuntimeException(e.getClass().getSimpleName() + " " + e.getMessage());
			} finally {

			}
		};

		// define a repeat schedule
		executor.scheduleAtFixedRate(r, initialDelay, period, TimeUnit.MILLISECONDS);
		TaskIds.put(id, executor);

		return id;

	}

	/**
	 * Execute a function periodically . <br>
	 * 
	 * This has the same functionality as the setInterval() function in JavaScript.​
	 * 
	 * @param task              the runnable task
	 * @param delayMilliseconds delay time (in milliseconds)
	 * @return return an id which can used in clearTimeout()
	 */
	public static int setInterval(Runnable task, long delayMilliseconds) {
		return run(task, delayMilliseconds, delayMilliseconds);
	}

	/**
	 * Execute a function after a specified delay (in milliseconds). <br>
	 * This has the same functionality as the setTimeout() function in JavaScript.​
	 * 
	 * @param task              the runnable task
	 * @param delayMilliseconds delay time (in milliseconds)
	 * @return return an id which can used in clearTimeout()
	 */
	public static int setTimeout(Runnable task, long delayMilliseconds) {
		// create an id
		final int id = TaskIds.newId();

		// create a executor
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

		// create a runnable
		Runnable r = () -> {
			try {
				if (TaskIds.exists(id)) {
					task.run();
					TaskIds.remove(id);
				}
			} catch (Exception e) {
				throw new RuntimeException(e.getClass().getSimpleName() + " " + e.getMessage());
			} finally {
				executor.shutdown();
			}
		};

		// define a schedule
		executor.schedule(r, delayMilliseconds, TimeUnit.MILLISECONDS);
		TaskIds.put(id, executor);
		executor.shutdown();

		return id;
	}

	/**
	 * Cancel a timeout task that previously established by calling setTimeout() or
	 * setInterval(). <br>
	 * This has the same functionality as the clearTimeout() function in
	 * JavaScript.​
	 * 
	 * @return return true if the id exists and removed.
	 */
	public static boolean clearTimeout(int id) {
		ScheduledExecutorService executor = TaskIds.get(id);
		try {
			if (executor != null)
				executor.shutdownNow();
		} catch (Exception e) {
		}
		return TaskIds.remove(id);
	}

	/** sleep a while */
	public static void sleep(long milliseconds) {
		try {
			TimeUnit.MILLISECONDS.sleep(milliseconds);
		} catch (InterruptedException e) {
		}
	}

	/** Create a Timer object */
	public static Timer start() {
		Timer ret = new Timer();
		ret.startTime = System.currentTimeMillis();
		return ret;
	}

	// ---------- members of Timer instance----------

	long startTime = 0;

	private int taskId;

	/** Check whether current is timeout */
	public boolean isTimeout(long timeoutMs) {
		return (System.currentTimeMillis() - startTime) < timeoutMs;
	}

	/** return time usage in milliseconds */
	public long timeUsage() {
		return System.currentTimeMillis() - startTime;
	}

	public LocalDateTime now() {
		return LocalDateTime.now();
	}

	/** convert time to timestamp(long value) */
	protected long timestamp(LocalDateTime time) {
		return LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	/** convert time to timestamp(long value) */
	protected long timestamp(Date time) {
		return time.getTime();
	}

	/**
	 * Schedules the specified task for execution after the specified delay.
	 *
	 * @param task  task to be scheduled.
	 * @param delay delay in milliseconds before task is to be executed.
	 * @throws IllegalArgumentException if {@code delay} is negative, or
	 *                                  {@code delay + System.currentTimeMillis()}
	 *                                  is negative.
	 * @throws IllegalStateException    if task was already scheduled or cancelled,
	 *                                  timer was cancelled, or timer thread
	 *                                  terminated.
	 * @throws NullPointerException     if {@code task} is null
	 */
	public void schedule(Runnable task, long delayMs) {
		taskId = setTimeout(task, delayMs);
	}

	/**
	 * Schedules the specified task for execution at the specified time. If the time
	 * is in the past, the task is scheduled for immediate execution.
	 *
	 * @param task task to be scheduled.
	 * @param time time at which task is to be executed.
	 * @throws IllegalArgumentException if {@code time.getTime()} is negative.
	 * @throws IllegalStateException    if task was already scheduled or cancelled,
	 *                                  timer was cancelled, or timer thread
	 *                                  terminated.
	 * @throws NullPointerException     if {@code task} or {@code time} is null
	 */
	public void schedule(Runnable task, Date time) {
		taskId = setTimeout(task, timestamp(time) - timestamp(now()));
	}

	/**
	 * Schedules the specified task for execution at the specified time. If the time
	 * is in the past, the task is scheduled for immediate execution.
	 *
	 * @param task task to be scheduled.
	 * @param time time at which task is to be executed.
	 * @throws IllegalArgumentException if {@code time.getTime()} is negative.
	 * @throws IllegalStateException    if task was already scheduled or cancelled,
	 *                                  timer was cancelled, or timer thread
	 *                                  terminated.
	 * @throws NullPointerException     if {@code task} or {@code time} is null
	 */
	public void schedule(Runnable task, LocalDateTime time) {
		taskId = setTimeout(task, timestamp(time) - timestamp(now()));
	}

	/**
	 * Schedules the specified task for repeated <i>fixed-delay execution</i>,
	 * beginning after the specified delay. Subsequent executions take place at
	 * approximately regular intervals separated by the specified period.
	 *
	 * <p>
	 * In fixed-delay execution, each execution is scheduled relative to the actual
	 * execution time of the previous execution. If an execution is delayed for any
	 * reason (such as garbage collection or other background activity), subsequent
	 * executions will be delayed as well. In the long run, the frequency of
	 * execution will generally be slightly lower than the reciprocal of the
	 * specified period (assuming the system clock underlying
	 * {@code Object.wait(long)} is accurate).
	 *
	 * <p>
	 * Fixed-delay execution is appropriate for recurring activities that require
	 * "smoothness." In other words, it is appropriate for activities where it is
	 * more important to keep the frequency accurate in the short run than in the
	 * long run. This includes most animation tasks, such as blinking a cursor at
	 * regular intervals. It also includes tasks wherein regular activity is
	 * performed in response to human input, such as automatically repeating a
	 * character as long as a key is held down.
	 *
	 * @param task   task to be scheduled.
	 * @param delay  delay in milliseconds before task is to be executed.
	 * @param period time in milliseconds between successive task executions.
	 * @throws IllegalArgumentException if {@code delay < 0}, or
	 *                                  {@code delay + System.currentTimeMillis() < 0},
	 *                                  or {@code period <= 0}
	 * @throws IllegalStateException    if task was already scheduled or cancelled,
	 *                                  timer was cancelled, or timer thread
	 *                                  terminated.
	 * @throws NullPointerException     if {@code task} is null
	 */
	public void schedule(Runnable task, long delay, long period) {
		taskId = run(task, delay, period);
	}

	/**
	 * Schedules the specified task for repeated <i>fixed-delay execution</i>,
	 * beginning at the specified time. Subsequent executions take place at
	 * approximately regular intervals, separated by the specified period.
	 *
	 * <p>
	 * In fixed-delay execution, each execution is scheduled relative to the actual
	 * execution time of the previous execution. If an execution is delayed for any
	 * reason (such as garbage collection or other background activity), subsequent
	 * executions will be delayed as well. In the long run, the frequency of
	 * execution will generally be slightly lower than the reciprocal of the
	 * specified period (assuming the system clock underlying
	 * {@code Object.wait(long)} is accurate). As a consequence of the above, if the
	 * scheduled first time is in the past, it is scheduled for immediate execution.
	 *
	 * <p>
	 * Fixed-delay execution is appropriate for recurring activities that require
	 * "smoothness." In other words, it is appropriate for activities where it is
	 * more important to keep the frequency accurate in the short run than in the
	 * long run. This includes most animation tasks, such as blinking a cursor at
	 * regular intervals. It also includes tasks wherein regular activity is
	 * performed in response to human input, such as automatically repeating a
	 * character as long as a key is held down.
	 *
	 * @param task      task to be scheduled.
	 * @param firstTime First time at which task is to be executed.
	 * @param period    time in milliseconds between successive task executions.
	 * @throws IllegalArgumentException if {@code firstTime.getTime() < 0}, or
	 *                                  {@code period <= 0}
	 * @throws IllegalStateException    if task was already scheduled or cancelled,
	 *                                  timer was cancelled, or timer thread
	 *                                  terminated.
	 * @throws NullPointerException     if {@code task} or {@code firstTime} is null
	 */
	public void schedule(Runnable task, Date firstTime, long period) {
		schedule(task, timestamp(firstTime) - timestamp(now()), period);
	}

	/**
	 * Schedules the specified task for repeated <i>fixed-rate execution</i>,
	 * beginning after the specified delay. Subsequent executions take place at
	 * approximately regular intervals, separated by the specified period.
	 *
	 * <p>
	 * In fixed-rate execution, each execution is scheduled relative to the
	 * scheduled execution time of the initial execution. If an execution is delayed
	 * for any reason (such as garbage collection or other background activity), two
	 * or more executions will occur in rapid succession to "catch up." In the long
	 * run, the frequency of execution will be exactly the reciprocal of the
	 * specified period (assuming the system clock underlying
	 * {@code Object.wait(long)} is accurate).
	 *
	 * <p>
	 * Fixed-rate execution is appropriate for recurring activities that are
	 * sensitive to <i>absolute</i> time, such as ringing a chime every hour on the
	 * hour, or running scheduled maintenance every day at a particular time. It is
	 * also appropriate for recurring activities where the total time to perform a
	 * fixed number of executions is important, such as a countdown timer that ticks
	 * once every second for ten seconds. Finally, fixed-rate execution is
	 * appropriate for scheduling multiple repeating timer tasks that must remain
	 * synchronized with respect to one another.
	 *
	 * @param task   task to be scheduled.
	 * @param delay  delay in milliseconds before task is to be executed.
	 * @param period time in milliseconds between successive task executions.
	 * @throws IllegalArgumentException if {@code delay < 0}, or
	 *                                  {@code delay + System.currentTimeMillis() < 0},
	 *                                  or {@code period <= 0}
	 * @throws IllegalStateException    if task was already scheduled or cancelled,
	 *                                  timer was cancelled, or timer thread
	 *                                  terminated.
	 * @throws NullPointerException     if {@code task} is null
	 */
	public void scheduleAtFixedRate(Runnable task, long delay, long period) {
		taskId = run(task, delay, period);
	}

	/**
	 * Schedules the specified task for repeated <i>fixed-rate execution</i>,
	 * beginning at the specified time. Subsequent executions take place at
	 * approximately regular intervals, separated by the specified period.
	 *
	 * <p>
	 * In fixed-rate execution, each execution is scheduled relative to the
	 * scheduled execution time of the initial execution. If an execution is delayed
	 * for any reason (such as garbage collection or other background activity), two
	 * or more executions will occur in rapid succession to "catch up." In the long
	 * run, the frequency of execution will be exactly the reciprocal of the
	 * specified period (assuming the system clock underlying
	 * {@code Object.wait(long)} is accurate). As a consequence of the above, if the
	 * scheduled first time is in the past, then any "missed" executions will be
	 * scheduled for immediate "catch up" execution.
	 *
	 * <p>
	 * Fixed-rate execution is appropriate for recurring activities that are
	 * sensitive to <i>absolute</i> time, such as ringing a chime every hour on the
	 * hour, or running scheduled maintenance every day at a particular time. It is
	 * also appropriate for recurring activities where the total time to perform a
	 * fixed number of executions is important, such as a countdown timer that ticks
	 * once every second for ten seconds. Finally, fixed-rate execution is
	 * appropriate for scheduling multiple repeating timer tasks that must remain
	 * synchronized with respect to one another.
	 *
	 * @param task      task to be scheduled.
	 * @param firstTime First time at which task is to be executed.
	 * @param period    time in milliseconds between successive task executions.
	 * @throws IllegalArgumentException if {@code firstTime.getTime() < 0} or
	 *                                  {@code period <= 0}
	 * @throws IllegalStateException    if task was already scheduled or cancelled,
	 *                                  timer was cancelled, or timer thread
	 *                                  terminated.
	 * @throws NullPointerException     if {@code task} or {@code firstTime} is null
	 */
	public void scheduleAtFixedRate(TimerTask task, Date firstTime, long period) {
		scheduleAtFixedRate(task, timestamp(firstTime) - timestamp(now()), period);
	}

	/** cancel current schedule task */
	public void cancel() {
		clearTimeout(taskId);
	}
}
