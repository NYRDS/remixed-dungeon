package com.nyrds.platform.storage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.StringWriter;
import java.io.StringReader;

public class HtmlPreferences {
    private static final String PREF_NAME = "RemixedDungeon";
    private static Preferences prefs;
    
    private static JsonObject prefData;
    private static boolean isDirty = false;

    static {
        prefs = Gdx.app.getPreferences(PREF_NAME);
        loadPreferences();
    }

    private static void loadPreferences() {
        try {
            String data = prefs.getString("data", "{}");
            prefData = JsonParser.parseString(data).getAsJsonObject();
        } catch (Exception e) {
            prefData = new JsonObject();
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
        JsonElement value = prefData.get(key);
        return value != null ? value.getAsBoolean() : defValue;
    }

    public static int getInt(String key, int defValue) {
        JsonElement value = prefData.get(key);
        return value != null ? value.getAsInt() : defValue;
    }

    public static long getLong(String key, long defValue) {
        JsonElement value = prefData.get(key);
        return value != null ? value.getAsLong() : defValue;
    }

    public static float getFloat(String key, float defValue) {
        JsonElement value = prefData.get(key);
        return value != null ? value.getAsFloat() : defValue;
    }

    public static String getString(String key, String defValue) {
        JsonElement value = prefData.get(key);
        return value != null ? value.getAsString() : defValue;
    }

    public static void putBoolean(String key, boolean value) {
        prefData.addProperty(key, value);
        isDirty = true;
    }

    public static void putInt(String key, int value) {
        prefData.addProperty(key, value);
        isDirty = true;
    }

    public static void putLong(String key, long value) {
        prefData.addProperty(key, value);
        isDirty = true;
    }

    public static void putFloat(String key, float value) {
        prefData.addProperty(key, value);
        isDirty = true;
    }

    public static void putString(String key, String value) {
        prefData.addProperty(key, value);
        isDirty = true;
    }

    public static void remove(String key) {
        prefData.remove(key);
        isDirty = true;
    }

    public static void clear() {
        prefData = new JsonObject();
        isDirty = true;
    }

    public static void flush() {
        savePreferences();
    }
}