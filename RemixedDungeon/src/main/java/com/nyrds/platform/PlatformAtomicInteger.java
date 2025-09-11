package com.nyrds.platform;

/**
 * A simple interface that abstracts the methods we use from AtomicInteger
 * to ensure compatibility with TeaVM.
 */
public interface PlatformAtomicInteger {
    int get();
    void set(int newValue);

    /**
     * Atomically increments the current value by one.
     * @return the updated value
     */
    int incrementAndGet();

    /**
     * Atomically decrements the current value by one.
     * @return the updated value
     */
    int decrementAndGet();
}