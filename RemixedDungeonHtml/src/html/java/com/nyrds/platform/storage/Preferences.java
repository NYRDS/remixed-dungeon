package com.nyrds.platform.storage;

/**
 * HTML version of Preferences
 */
public enum Preferences {

    INSTANCE;

    public static boolean contains(String key) {
        return HtmlPreferences.contains(key);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return HtmlPreferences.getBoolean(key, defValue);
    }

    public static int getInt(String key, int defValue) {
        return HtmlPreferences.getInt(key, defValue);
    }

    public static long getLong(String key, long defValue) {
        return HtmlPreferences.getLong(key, defValue);
    }

    public static float getFloat(String key, float defValue) {
        return HtmlPreferences.getFloat(key, defValue);
    }

    public static double getDouble(String key, double defValue) {
        return HtmlPreferences.getFloat(key, (float) defValue);
    }

    public static String getString(String key, String defValue) {
        return HtmlPreferences.getString(key, defValue);
    }

    public static void putBoolean(String key, boolean value) {
        HtmlPreferences.putBoolean(key, value);
    }

    public static void putInt(String key, int value) {
        HtmlPreferences.putInt(key, value);
    }

    public static void putLong(String key, long value) {
        HtmlPreferences.putLong(key, value);
    }

    public static void putFloat(String key, float value) {
        HtmlPreferences.putFloat(key, value);
    }

    public static void putDouble(String key, double value) {
        HtmlPreferences.putFloat(key, (float) value);
    }

    public static void putString(String key, String value) {
        HtmlPreferences.putString(key, value);
    }

    // Missing put methods with dummy stubs
    public static void put(String key, boolean value) {
        putBoolean(key, value);
    }

    public static void put(String key, int value) {
        putInt(key, value);
    }

    public static void put(String key, double value) {
        putDouble(key, value);
    }

    public static void put(String key, String value) {
        putString(key, value);
    }

    public static void put(String key, Boolean value) {
        if (value != null) {
            putBoolean(key, value);
        }
    }

    public static void put(String key, float value) {
        putFloat(key, value);
    }

    public static void remove(String key) {
        HtmlPreferences.remove(key);
    }

    public static void clear() {
        HtmlPreferences.clear();
    }

    public static void flush() {
        HtmlPreferences.flush();
    }
}