package com.nyrds.platform.storage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import org.json.JSONObject;

import java.util.Iterator;

public class HtmlPreferences {
    private static final String PREF_NAME = "RemixedDungeon";
    private static Preferences prefs;
    
    private static JSONObject prefData;
    private static boolean isDirty = false;

    static {
        prefs = Gdx.app.getPreferences(PREF_NAME);
        loadPreferences();
    }

    private static void loadPreferences() {
        try {
            String data = prefs.getString("data", "{}");
            prefData = new JSONObject(data);
        } catch (Exception e) {
            prefData = new JSONObject();
        }
    }

    private static void savePreferences() {
        if (isDirty) {
            try {
                String jsonData = prefData.toString();
                prefs.putString("data", jsonData);
                prefs.flush();
                isDirty = false;
            } catch (Exception e) {
                // Error saving preferences
            }
        }
    }

    public static boolean contains(String key) {
        return prefData.has(key);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return prefData.optBoolean(key, defValue);
    }

    public static int getInt(String key, int defValue) {
        return prefData.optInt(key, defValue);
    }

    public static long getLong(String key, long defValue) {
        return prefData.optLong(key, defValue);
    }

    public static float getFloat(String key, float defValue) {
        double val = prefData.optDouble(key, defValue);
        return (float) val;
    }

    public static String getString(String key, String defValue) {
        return prefData.optString(key, defValue);
    }

    public static void putBoolean(String key, boolean value) {
        prefData.put(key, value);
        isDirty = true;
    }

    public static void putInt(String key, int value) {
        prefData.put(key, value);
        isDirty = true;
    }

    public static void putLong(String key, long value) {
        prefData.put(key, value);
        isDirty = true;
    }

    public static void putFloat(String key, float value) {
        prefData.put(key, value);
        isDirty = true;
    }

    public static void putString(String key, String value) {
        prefData.put(key, value);
        isDirty = true;
    }

    public static void remove(String key) {
        prefData.remove(key);
        isDirty = true;
    }

    public static void clear() {
        prefData = new JSONObject();
        isDirty = true;
    }

    public static void flush() {
        savePreferences();
    }
}