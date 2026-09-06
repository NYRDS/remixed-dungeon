package com.nyrds.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Minimal Future standing in for FutureTask, which TeaVM's classlib does not
 * provide. Runs the wrapped task exactly once via run(); completed inline by
 * SingleThreadedExecutor on html, queued by the ThreadPoolExecutor elsewhere.
 */
class SimpleFuture<T> implements Future<T>, Runnable {

	private final Runnable task;
	private final T       value;

	private volatile boolean done;
	private volatile boolean cancelled;
	private       Throwable  error;

	Throwable getError() {
		return error;
	}

	SimpleFuture(Runnable task, T value) {
		this.task = task;
		this.value = value;
	}

	@Override
	public void run() {
		if (done) {
			return;
		}
		try {
			task.run();
		} catch (Throwable t) {
			error = t;
		}
		done = true;
		synchronized (this) {
			notifyAll();
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (done) {
			return false;
		}
		cancelled = true;
		done = true;
		synchronized (this) {
			notifyAll();
		}
		return true;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		synchronized (this) {
			while (!done) {
				wait();
			}
		}
		if (error != null) {
			throw new ExecutionException(error);
		}
		return value;
	}

	// narrower throws than Future.get(long,TimeUnit): TeaVM's classlib has no
	// TimeoutException, and this class must link there
	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
		long millis = unit.toMillis(timeout);
		synchronized (this) {
			while (!done && millis > 0) {
				long start = System.currentTimeMillis();
				wait(millis);
				millis -= System.currentTimeMillis() - start;
			}
		}
		if (!done) {
			throw new ExecutionException(new RuntimeException("SimpleFuture timed out"));
		}
		if (error != null) {
			throw new ExecutionException(error);
		}
		return value;
	}
}
