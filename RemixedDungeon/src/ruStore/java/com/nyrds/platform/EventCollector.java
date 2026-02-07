package com.nyrds.platform;

import com.nyrds.platform.storage.CommonPrefs;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

public class EventCollector {
    public static void logException() {
        logException(new Exception(), 1);
    }

    public static void logException(Exception e, String s) {
    }

    public static void logException(Throwable e) {
    }

    public static void collectSessionData(String freeInternalMemorySize, String toString) {
    }

    public static void logException(String s) {
    }

    static private void logException(Throwable e, int level) {
        // Empty implementation for ruStore
    }

    public static void logEvent(String s) {
    }

    public static void logEvent(String s1, String s2) {
    }


    public static void logEvent(String gameover, Map<String, String> resDesc) {
    }

    public static void levelUp(String s, int lvl) {
    }

    public static void badgeUnlocked(String name) {
    }

    public static void logEvent(String survey, String questionString, String answer) {
    }

    public static void disable() {
    }

    public static void logCountedEvent(String ad_reward5, int i) {
    }

    public static void logScene(String s) {
    }

    public static void setSessionData(String rpd_active_mod, String activeMod) {
    }
}
