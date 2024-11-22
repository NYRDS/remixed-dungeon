package com.nyrds.platform.util;

import java.io.File;

public class Os {
    public static long getAvailableInternalMemorySize() {
        File path = new File(System.getProperty("user.dir"));
        return path.getUsableSpace();
    }
}