package com.nyrds.platform;

import com.nyrds.pixeldungeon.ml.BuildConfig;
import java.util.Map;

public class EventCollector {
    public static void logEvent(String event) {
        // Event collection is not supported in HTML build
        System.out.println("Event: " + event);
    }

    public static void logEvent(String event, String param) {
        // Event collection is not supported in HTML build
        System.out.println("Event: " + event + ", Param: " + param);
    }
    
    public static void logEvent(String event, String param1, String param2) {
        // Event collection is not supported in HTML build
        System.out.println("Event: " + event + ", Param1: " + param1 + ", Param2: " + param2);
    }

    public static void logException(Exception e) {
        // Exception logging is not supported in HTML build
        System.err.println("Exception: " + e.getMessage());
        e.printStackTrace();
    }
    
    // Overloaded method with additional parameter - changed to accept Object
    public static void logException(Object e) {
        // Exception logging is not supported in HTML build
        if (e instanceof Exception) {
            Exception ex = (Exception) e;
            System.err.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
        } else {
            System.err.println("Exception: " + e.toString());
        }
    }
    
    // Overloaded method with additional parameter - changed to accept Object
    public static void logException(Object e, String param) {
        // Exception logging is not supported in HTML build
        if (e instanceof Exception) {
            Exception ex = (Exception) e;
            System.err.println("Exception: " + ex.getMessage() + ", Param: " + param);
            ex.printStackTrace();
        } else {
            System.err.println("Exception: " + e.toString() + ", Param: " + param);
        }
    }
    
    public static void logException(Throwable e) {
        // Exception logging is not supported in HTML build
        System.err.println("Exception: " + e.getMessage());
        e.printStackTrace();
    }
    
    public static void logException(Throwable e, String desc) {
        // Exception logging is not supported in HTML build
        System.err.println("Exception: " + e.getMessage() + ", Desc: " + desc);
        e.printStackTrace();
    }

    public static void logScene(String scene) {
        // Scene logging is not supported in HTML build
        System.out.println("Scene: " + scene);
    }
    
    public static void levelUp(String character, long level) {
        // Level up logging is not supported in HTML build
        System.out.println("LevelUp: " + character + ", Level: " + level);
    }
    
    public static void badgeUnlocked(String badgeId) {
        // Badge unlocked logging is not supported in HTML build
        System.out.println("BadgeUnlocked: " + badgeId);
    }
    
    public static void logEvent(String category, Map<String, String> eventData) {
        // Event collection is not supported in HTML build
        System.out.println("Event: " + category + ", Data: " + eventData);
    }
    
    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    public static void autoTrack(String event) {
        // Auto tracking is not supported in HTML build
        System.out.println("AutoTrack: " + event);
    }
    
    // Additional methods needed for HTML version
    public static void setSessionData(String key, String value) {
        // No analytics on web, but mirror the session state into the page so the
        // browser debug loop can see the current scene etc. Deliberately not
        // printed: LuaScript calls this every tick.
        setJsState(key, value);
    }

    public static void setSessionData(String key, boolean value) {
        setJsState(key, Boolean.toString(value));
    }

    // browser debug state, read via window.__gameState
    @org.teavm.jso.JSBody(params = {"k", "v"}, script =
            "var s = window.__gameState || (window.__gameState = {}); s[k] = v;")
    private static native void setJsState(String key, String value);
    
    public static void disable() {
        // Disable analytics is not supported in HTML build
        System.out.println("Analytics disabled");
    }
    
    // Method needed for MovieRewardTask
    public static void logCountedEvent(String event, int count) {
        // Counted event logging is not supported in HTML build
        System.out.println("CountedEvent: " + event + ", Count: " + count);
    }
}