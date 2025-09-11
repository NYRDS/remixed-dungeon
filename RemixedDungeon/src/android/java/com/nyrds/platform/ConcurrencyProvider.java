package com.nyrds.platform;

import com.nyrds.util.ReportingExecutor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyProvider {
    private static class AtomicIntegerWrapper implements PlatformAtomicInteger {
        private final AtomicInteger atomicInteger;

        public AtomicIntegerWrapper(int initialValue) {
            this.atomicInteger = new AtomicInteger(initialValue);
        }

        @Override
        public int get() {
            return atomicInteger.get();
        }

        @Override
        public void set(int newValue) {
            atomicInteger.set(newValue);
        }

        @Override
        public int incrementAndGet() {
            return atomicInteger.incrementAndGet(); // Add this line
        }

        @Override
        public int decrementAndGet() {
            return atomicInteger.decrementAndGet(); // Add this line
        }
    }

    public ReportingExecutor createReportingExecutor() {
        return new ReportingExecutor();
    }

    public <T> Queue<T> createConcurrentLinkedQueue() {
        return new ConcurrentLinkedQueue<>();
    }

    public PlatformAtomicInteger createAtomicInteger(int initialValue) {
        return new AtomicIntegerWrapper(initialValue);
    }

    public Object createLock() {
        return new Object();
    }
}