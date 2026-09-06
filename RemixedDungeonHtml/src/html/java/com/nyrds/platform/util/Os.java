package com.nyrds.platform.util;

import java.io.File;

public class Os {
    public static long getAvailableInternalMemorySize() {
        // user.dir is undefined on TeaVM and File.getUsableSpace() is a stub
        // returning Long.MAX_VALUE anyway
        String dir = System.getProperty("user.dir");
        if (dir == null) {
            return Long.MAX_VALUE;
        }
        return new File(dir).getUsableSpace();
    }

    public static boolean isConnectedToInternet() {
        return true;
    }
}