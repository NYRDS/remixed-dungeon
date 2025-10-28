package com.nyrds.platform.util;

import java.io.File;

public class Os {
    public static long getAvailableInternalMemorySize() {
        // Check if SNAP_USER_DATA is available via user.home system property
        String snapUserData = System.getProperty("user.home");
        if (snapUserData != null && !snapUserData.isEmpty()) {
            File path = new File(snapUserData);
            return path.getUsableSpace();
        }

        // Fallback to the current directory if user.home is not set
        File path = new File(System.getProperty("user.dir"));
        return path.getUsableSpace();
    }

    public static boolean isConnectedToInternet() {
        return true;
    }
}