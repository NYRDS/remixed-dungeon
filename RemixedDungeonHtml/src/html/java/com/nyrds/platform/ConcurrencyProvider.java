package com.nyrds.platform;

import com.nyrds.util.ReportingExecutor; // Assuming this can be adapted or reimplemented

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

// A simple, single-threaded executor
class SingleThreadedExecutor extends ReportingExecutor {
    @Override
    public void execute(Runnable command) {
        command.run();
    }
}

public class ConcurrencyProvider {

    private static class SingleThreadedAtomicInteger implements PlatformAtomicInteger {
        private int value;

        public SingleThreadedAtomicInteger(int initialValue) {
            this.value = initialValue;
        }

        @Override
        public int get() {
            return value;
        }

        @Override
        public void set(int newValue) {
            this.value = newValue;
        }

        @Override
        public int incrementAndGet() {
            return ++value; // Add this line
        }

        @Override
        public int decrementAndGet() {
            return --value; // Add this line
        }
    }

    public ReportingExecutor createReportingExecutor() {
        return new SingleThreadedExecutor();
    }


    public <T> Queue<T> createConcurrentLinkedQueue() {
        return new ArrayDeque<>();
    }

    public PlatformAtomicInteger createAtomicInteger(int initialValue) {
        return new SingleThreadedAtomicInteger(initialValue);
    }


    public Object createLock() {
        return new Object(); // A plain object is sufficient for single-threaded locks.
    }
}