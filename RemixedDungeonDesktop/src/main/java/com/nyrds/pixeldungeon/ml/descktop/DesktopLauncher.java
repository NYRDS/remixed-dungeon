package com.nyrds.pixeldungeon.ml.descktop;

import com.watabou.utils.SystemTime;

public class DesktopLauncher {
    public static void main(String[] args) {
        SystemTime.tick();
        System.out.println("Hello world! " + SystemTime.now());
    }
}