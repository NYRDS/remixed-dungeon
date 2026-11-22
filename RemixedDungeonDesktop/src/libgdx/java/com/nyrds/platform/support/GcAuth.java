package com.nyrds.platform.support;

import com.nyrds.platform.app.RemixedDungeonApp;

/**
 * VK Play GameCenter auth forwarding ticket (see docs "GC Authorization").
 * The GameCenter launches the game with --sz_pers_id / --sz_token when
 * "Advanced client settings" enable authorization passthrough.
 */
public class GcAuth {
    public static boolean hasTicket() {
        return RemixedDungeonApp.hasGcTicket();
    }

    public static String persId() {
        return RemixedDungeonApp.gcPersId();
    }

    public static String otp() {
        return RemixedDungeonApp.gcToken();
    }
}
