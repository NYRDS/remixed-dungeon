package com.nyrds.platform.util;

public class Os {
    public static long getAvailableInternalMemorySize() {
        // TeaVM/HTML build: no filesystem access, return a large default value
        return 1024L * 1024L * 1024L; // 1GB
    }

    public static boolean isConnectedToInternet() {
        return true;
    }
}