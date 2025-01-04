package com.nyrds.platform.storage;

import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.Stringify;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public enum Preferences {

	INSTANCE;

	private final Map<String, Integer> intCache = new HashMap<>();
	private final Map<String, String> stringCache = new HashMap<>();
	private final Map<String, Boolean> boolCache = new HashMap<>();
	private final Map<String, Double> doubleCache = new HashMap<>();

	private final File preferencesFile = new File("preferences.hjson");
	private JsonObject preferencesJson;

	Preferences() {
		loadPreferences();
	}

	private void loadPreferences() {
		if (preferencesFile.exists()) {
			try (FileReader reader = new FileReader(preferencesFile)) {
				preferencesJson = JsonValue.readHjson(reader).asObject();
			} catch (IOException e) {
				preferencesJson = new JsonObject();
			}
		} else {
			preferencesJson = new JsonObject();
		}
	}

	private void savePreferences() {
		try (FileWriter writer = new FileWriter(preferencesFile)) {
			preferencesJson.writeTo(writer, Stringify.HJSON);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getInt(String key, int defValue) {
		if (intCache.containsKey(key)) {
			return intCache.get(key);
		}

		int value = preferencesJson.getInt(key, defValue);
		intCache.put(key, value);
		return value;
	}

	public double getDouble(String key, double defValue) {
		if (doubleCache.containsKey(key)) {
			return doubleCache.get(key);
		}

		double value = preferencesJson.getDouble(key, defValue);
		doubleCache.put(key, value);
		return value;
	}

	public boolean getBoolean(String key, boolean defValue) {
		if (boolCache.containsKey(key)) {
			return boolCache.get(key);
		}

		boolean value = preferencesJson.getBoolean(key, defValue);
		boolCache.put(key, value);
		return value;
	}

	public String getString(String key, String defValue) {
		if (stringCache.containsKey(key)) {
			return stringCache.get(key);
		}

		String value = preferencesJson.getString(key, defValue);
		stringCache.put(key, value);
		return value;
	}

	public void put(String key, int value) {
		intCache.put(key, value);
		preferencesJson.set(key, value);
		savePreferences();
	}

	public void put(String key, double value) {
		doubleCache.put(key, value);
		preferencesJson.set(key, value);
		savePreferences();
	}

	public void put(String key, boolean value) {
		boolCache.put(key, value);
		preferencesJson.set(key, value);
		savePreferences();
	}

	public void put(String key, String value) {
		stringCache.put(key, value);
		preferencesJson.set(key, value);
		savePreferences();
	}
}