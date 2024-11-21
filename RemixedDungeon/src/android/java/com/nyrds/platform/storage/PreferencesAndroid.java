package com.nyrds.platform.storage;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.nyrds.platform.events.EventCollector;
import com.nyrds.util.UserKey;
import com.nyrds.util.Utils;

import java.util.Map;

public class PreferencesAndroid implements IPreferences {

    private SharedPreferences prefs;

    public PreferencesAndroid(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    private boolean checkString(String key) {
        try {
            prefs.getString(key, Utils.EMPTY_STRING);
        } catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    private boolean checkInt(String key) {
        try {
            prefs.getInt(key, 0);
        } catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    private boolean checkBoolean(String key) {
        try {
            prefs.getBoolean(key, false);
        } catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    public String getString(String key, String defValue, Map<String, String> stringCache) {

        if (stringCache.containsKey(key)) {
            return stringCache.get(key);
        }

        String value;

        try {
            String scrambledKey = UserKey.encrypt(key);

            if (prefs.contains(scrambledKey)) {
                String encVal = prefs.getString(scrambledKey, UserKey.encrypt(defValue));
                value = UserKey.decrypt(encVal);
                stringCache.put(key, value);
                return value;
            }

            if (prefs.contains(key)) {
                String val = Utils.EMPTY_STRING;

                if (checkString(key)) {
                    val = prefs.getString(key, defValue);
                }
                if (checkInt(key)) {
                    val = Integer.toString(prefs.getInt(key, Integer.parseInt(defValue)));
                }
                if (checkBoolean(key)) {
                    val = Boolean.toString(prefs.getBoolean(key, Boolean.parseBoolean(defValue)));
                }

                prefs.edit().putString(scrambledKey, UserKey.encrypt(val)).apply();
                prefs.edit().remove(key).apply();
                return val;
            }
        } catch (ClassCastException e) {
            //just return default value when loading old preferences
        } catch (Exception e) {
            EventCollector.logException(e);
        }
        return defValue;
    }

    @SuppressLint("ApplySharedPref")
    public void put(String key, String value, Map<String, String> stringCache) {

        stringCache.put(key, value);

        String scrambledVal = UserKey.encrypt(value);
        String scrambledKey = UserKey.encrypt(key);

        if (!prefs.edit().putString(scrambledKey, scrambledVal).commit()) {
            EventCollector.logException("Preferences commit failed");
        }
    }
}
