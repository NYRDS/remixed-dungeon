
package com.nyrds.platform.storage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.util.UserKey;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public enum Preferences {

	INSTANCE;

    private SharedPreferences prefs;

	private final Map<String, Integer> intCache    = new HashMap<>();
	private final Map<String, String>  stringCache = new HashMap<>();
	private final Map<String, Boolean> boolCache   = new HashMap<>();
	private final Map<String, Double>  doubleCache = new HashMap<>();

	public SharedPreferences get() {
		if (prefs == null) {
			prefs = RemixedDungeonApp.getContext().getSharedPreferences("com.watabou.pixeldungeon.RemixedDungeon", Activity.MODE_PRIVATE);
		}
		return prefs;
	}

	public int getInt(String key, int defValue) {

		if(intCache.containsKey(key)) {
			return intCache.get(key);
		}

		String defVal = Integer.toString(defValue);
		String propVal = getString(key, defVal);
		int value;
		try {
			value = Integer.parseInt(propVal);
		} catch (NumberFormatException e) {
			put(key, defValue);
			value = defValue;
		}
		intCache.put(key,value);
		return value;
	}

	public double getDouble(String key, double defValue) {

		if(doubleCache.containsKey(key)) {
			return doubleCache.get(key);
		}

		String defVal = Double.toString(defValue);
		String propVal = getString(key, defVal);

		double value;
		try {
			value = Double.parseDouble(propVal);
		} catch (NumberFormatException e) {
			put(key, defValue);
			value = defValue;
		}

		doubleCache.put(key, value);
		return value;
	}

	public boolean getBoolean(String key, boolean defValue) {

		if(boolCache.containsKey(key)) {
			return boolCache.get(key);
		}

		String defVal = Boolean.toString(defValue);
		String propVal = getString(key, defVal);
		boolean value = Boolean.parseBoolean(propVal);
		boolCache.put(key, value);
		return value;
	}

	public boolean checkString(String key) {
		try {
			get().getString(key, Utils.EMPTY_STRING);
		} catch (ClassCastException e) {
			return false;
		}
		return true;
	}

	boolean checkInt(String key) {
		try {
			get().getInt(key, 0);
		} catch (ClassCastException e) {
			return false;
		}
		return true;
	}

	public boolean checkBoolean(String key) {
		try {
			get().getBoolean(key, false);
		} catch (ClassCastException e) {
			return false;
		}
		return true;
	}

	public String getString(String key, String defValue) {

		if(stringCache.containsKey(key)) {
			return stringCache.get(key);
		}

		String value;

		try {
			String scrambledKey = UserKey.encrypt(key);

			if (get().contains(scrambledKey)) {
				String encVal = get().getString(scrambledKey, UserKey.encrypt(defValue));
				value = UserKey.decrypt(encVal);
				stringCache.put(key,value);
				return value;
			}

			if (get().contains(key)) {
				String val = Utils.EMPTY_STRING;

				if (checkString(key)) {
					val = get().getString(key, defValue);
				}
				if (checkInt(key)) {
					val = Integer.toString(get().getInt(key, Integer.parseInt(defValue)));
				}
				if (checkBoolean(key)) {
					val = Boolean.toString(get().getBoolean(key, Boolean.parseBoolean(defValue)));
				}

				get().edit().putString(scrambledKey, UserKey.encrypt(val)).apply();
				get().edit().remove(key).apply();
				return val;
			}
		} catch (ClassCastException e) {
			//just return default value when loading old preferences
		} catch (Exception e) {
			EventCollector.logException(e);
		}
		return defValue;
	}

	public void put(String key, int value) {

		intCache.put(key,value);

		String val = Integer.toString(value);
		put(key, val);
	}

	public void put(String key, double value) {

		doubleCache.put(key,value);

		String val = Double.toString(value);
		put(key, val);
	}

	public void put(String key, boolean value) {

		boolCache.put(key,value);

		String val = Boolean.toString(value);
		put(key, val);
	}

	@SuppressLint("ApplySharedPref")
	public void put(String key, String value) {

		stringCache.put(key, value);

		String scrambledVal = UserKey.encrypt(value);
		String scrambledKey = UserKey.encrypt(key);

		if(!get().edit().putString(scrambledKey, scrambledVal).commit()) {
			EventCollector.logException("Preferences commit failed");
		}
	}
}
