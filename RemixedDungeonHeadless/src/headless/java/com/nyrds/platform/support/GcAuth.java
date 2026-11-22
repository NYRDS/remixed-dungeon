package com.nyrds.platform.support;

/**
 * GameCenter auth forwarding exists only in the desktop (VK Play) build.
 */
public class GcAuth {
    public static boolean hasTicket() {
        return false;
    }

    public static String persId() {
        return "";
    }

    public static String otp() {
        return "";
    }
}
