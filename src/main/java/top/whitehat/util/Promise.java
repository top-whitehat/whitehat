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

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A Class like Promise in JavaScript
 * 
 * <h3>Usage example:</h3>
 * 
 * <pre>
 * // Create a promise
 * Promise promise = new Promise((resolve, reject) -> {
 * 	Promise.sleep(1000); // Simulate a time-consuming operation
 * 
 * 	// create a random number
 * 	Random rnd = new java.util.Random();
 * 	int number = rnd.nextInt(100);
 * 
 * 	if (number < 70) {
 * 		resolve.accept("hello");
 * 	} else {
 * 		reject.accept("reject inside the promise");
 * 	}
 * });
 * 
 * // define process chain for the promise
 * promise.then(e -> {
 * 	System.out.println("first  then, value = " + e);
 * 	return "" + e + " world";
 * }).then(e -> {
 * 	System.out.println("second then, value = " + e);
 * 	throw new RuntimeException("exception in then()");
 * }).catchError(err -> {
 * 	System.out.println("error occurs, error = " + err.getMessage());
 * 	return err;
 * }).finallyDo(() -> {
 * 	System.out.println("finally do something");
 * });
 * </pre>
 */
public class Promise {

	public final static String PENDING = "pending";
	public final static String FULFILLED = "fulfilled";
	public final static String REJECTED = "rejected";

	/** sleep a while */
	public static void sleep(int milliSeconds) {
		try {
			Thread.sleep(milliSeconds);
		} catch (InterruptedException e) {
		}
	}

	/** Future object of this promise */
	private final CompletableFuture<Object> future;

	/** result object of this promise */
	private Object result = null;

	/** error object of this promise */
	private Object error = null;

	/** private constructor for internal usage */
	private Promise(CompletableFuture<Object> future) {
		this.future = future;
	}

	/** private constructor for internal usage */
	private Promise(CompletableFuture<Object> future, Object value) {
		this.future = future;
		if (this.future.isCompletedExceptionally())
			this.error = value;
		else if (this.future.isDone())
			this.result = value;
	}

	/**
	 * Creates a Promise
	 * 
	 * @param executor
	 */
	public Promise(BiConsumer<Consumer<Object>, Consumer<Object>> executor) {
		this.future = new CompletableFuture<>();

		Consumer<Object> resolve = e -> {
			result = e;
			future.complete(e);
		};

		Consumer<Object> reject = err -> {
			error = err;
			future.completeExceptionally(new RuntimeException(err == null ? null : err.toString()));
		};

		try {
			executor.accept(resolve, reject);
		} catch (Exception e) {
			reject.accept(e);
		}
	}

	/** get current Future object */
	public CompletableFuture<Object> getFuture() {
		return this.future;
	}

	/** get result */
	public Object getResult() {
		return this.result;
	}

	/** get error */
	public Object getError() {
		return this.error;
	}

	/** get status */
	public String getStatus() {
		if (future.isDone()) {
			return future.isCompletedExceptionally() ? REJECTED : FULFILLED;
		} else {
			return PENDING;
		}
	}

	/**
	 * then() method
	 * 
	 * @param fn
	 * @return
	 */
	public Promise then(Function<Object, Object> fn) {
		return new Promise(future.thenApply(fn));
	}

	/**
	 * catch() method
	 */
	public Promise catchError(Function<Throwable, Object> errorHandler) {
		return new Promise(future.exceptionally(errorHandler));
	}

	/**
	 * finally() method
	 * 
	 * @param action
	 */
	public Promise finallyDo(Runnable action) {
		return new Promise(future.whenComplete((result, error) -> action.run()));
	}

	protected String getResultTetxt() {
		if (future.isDone()) {
			Object val = future.isCompletedExceptionally() ? error : result;
			return val == null ? "null" : val.toString();
		} else {
			return "";
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append("[");
		sb.append(getStatus());
		sb.append(" ");
		sb.append(getResultTetxt());
		sb.append("]");
		return sb.toString();
	}

	// --------------- static methods ---------------

	/**
	 * Create a resolved Promise
	 */
	public static Promise resolve(Object value) {
		return new Promise(CompletableFuture.completedFuture(value), value);
	}

	/**
	 * Create a rejected Promise
	 */
	public static Promise reject(Object err) {
		CompletableFuture<Object> future = new CompletableFuture<>();
		future.completeExceptionally(new RuntimeException(err == null ? null : err.toString()));
		return new Promise(future, err);
	}

	/**
	 * Create a new Promise that is completed when all of the given Promise complete.
	 * 
	 * @param promises some Promise Objects
	 * 
	 * @return a new Promise that is completed when all of the given Promise complete.
	 */
	public static Promise all(Promise... promises) {
		 CompletableFuture<?>[] futures = Arrays.stream(promises)
	                .map(p -> p.future)
	                .toArray(CompletableFuture<?>[]::new);

		// Create a new CompletableFuture that is completed when all of 
    	// the given CompletableFutures complete
		CompletableFuture<Object> future = CompletableFuture.allOf(futures) //
				.thenApply(v -> { //
					return Arrays.stream(promises) //
							.map(p -> p.future.join()) //
							.collect(Collectors.toList());
				});

		return new Promise(future);
	}
	
	/** Create a new Promise that is completed when any of the given 
	 *  Promise complete.
	 * 
	 * @param promises  the Promises
	 * @return a new Promise that is completed when any of the given Promise complete.
	 */
	public static Promise race(Promise... promises) {
		CompletableFuture<?>[] futures = Arrays.stream(promises)
                .map(p -> p.future)
                .toArray(CompletableFuture<?>[]::new);

		CompletableFuture<Object> future = CompletableFuture.anyOf(futures);
		
		return new Promise(future);		
	}

}
