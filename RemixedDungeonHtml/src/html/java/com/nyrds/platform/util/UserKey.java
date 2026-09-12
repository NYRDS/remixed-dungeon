package com.nyrds.platform.util;

import java.util.UUID;

/**
 * HTML version of UserKey
 */
public class UserKey {
    private static String userId;
    
    public static String get() {
        if (userId == null) {
            userId = UUID.randomUUID().toString();
        }
        return userId;
    }
    
    public static void set(String id) {
        userId = id;
    }
    
    public static int someValue() {
        get(); // lazily create userId like the android variant does
        return userId.hashCode();
    }
}