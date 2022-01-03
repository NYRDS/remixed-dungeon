package com.nyrds.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.SneakyThrows;

public class ReportingExecutor extends ThreadPoolExecutor {
    public ReportingExecutor() {
        super(1, // core threads
                1, // max threads
                1, // timeout
                TimeUnit.MINUTES, // timeout units
                new LinkedBlockingQueue<>(), // work queue
                new ThreadFactory() {
                    @Override
                    public Thread newThread(@NotNull Runnable r) {
                        Thread thread =  new Thread(r, "ReportingExecutor: "+this.hashCode());
                        thread.setPriority(Thread.MIN_PRIORITY);
                        return thread;
                    }
                }
        );
    }

    @SneakyThrows
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
                if (future.isDone()) {
                    future.get();
                }
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            throw t;
        }
    }

}
