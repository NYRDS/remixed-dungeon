package com.nyrds.platform;

import com.nyrds.platform.storage.Preferences;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;


public class EventCollector {

    private static boolean analyticsUsable() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_COLLECT_STATS, 1) > 0;
    }

    static public void init() {

    }

    static public void logCountedEvent(String event, int threshold) {
        final String key = "CountedEvent_" + event;
        int count = Preferences.INSTANCE.getInt(key, 0);
        count++;

        if (count == threshold) {
            logEvent(event);
        }
        Preferences.INSTANCE.put(key, count);
    }

    static public void logEvent(String event) {

    }

    static public void logEvent(String event, double value) {

    }

    static public void logEvent(String category, String event) {

    }

    static public void levelUp(String character, long level) {

    }

    static public void badgeUnlocked(String badgeId) {

    }

    static public void logEvent(String category, Map<String, String> eventData) {

    }

    static public void logEvent(String category, String event, String label) {

    }

    static public void logScene(final String scene) {
    }

    static public void logException() {
        logException(new Exception(), 1);
    }

    static public void logException(String desc) {
        logException(new Exception(desc), 1);
    }

    static private void logException(Throwable e, int level) {

        StackTraceElement[] stackTraceElements = e.getStackTrace();
        e.setStackTrace(Arrays.copyOfRange(stackTraceElements, level, stackTraceElements.length));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        ps.close();
        GLog.toFile(baos.toString());

        if (Util.isDebug()) {
            throw new RuntimeException(new Exception(e));
        }
    }

    static public void logException(Throwable e) {
        logException(e, 0);
    }

    static public void logException(Throwable e, String desc) {
        logException(e, 0);
    }

    public static void setSessionData(String key, boolean value) {

    }

    public static void setSessionData(String key, String value) {
    }

    public static void disable() {
    }
}
