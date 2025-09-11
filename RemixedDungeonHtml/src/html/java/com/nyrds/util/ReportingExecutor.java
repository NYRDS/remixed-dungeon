package com.nyrds.util;

import com.nyrds.platform.EventCollector;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

/**
 * TeaVM-specific implementation of ReportingExecutor that executes tasks synchronously
 * since TeaVM is single-threaded and doesn't support multi-threading.
 */
public class ReportingExecutor {
    public ReportingExecutor() {
        // TeaVM is single-threaded, so we don't need to create any threads
    }

    /**
     * Execute the task synchronously since TeaVM is single-threaded
     */
    public void execute(@NotNull Runnable command) {
        try {
            command.run();
        } catch (Exception e) {
            if (e instanceof Exception) {
                EventCollector.logException((Exception) e, this.toString());
            } else {
                EventCollector.logException(e.toString());
            }
        }
    }

    public Future<?> submit(Runnable task) {
        return null;
    }
}