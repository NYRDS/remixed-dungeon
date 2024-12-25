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

	public static final String KEY_IMMERSIVE = "immersive";
	public static final String KEY_MUSIC = "music";
	public static final String KEY_SOUND_FX = "soundfx";
	public static final String KEY_ZOOM = "zoom";
	public static final String KEY_LAST_CLASS = "last_class";
	public static final String KEY_DONATED = "donated";
	public static final String KEY_INTRO = "intro";
	public static final String KEY_BRIGHTNESS = "brightness";
	public static final String KEY_LOCALE = "locale";
	public static final String KEY_QUICKSLOTS = "quickslots";
	public static final String KEY_VERSION = "version";
	public static final String KEY_FONT_SCALE = "font_scale";
	public static final String KEY_CLASSIC_FONT = "classic_font";
	public static final String KEY_PREMIUM_SETTINGS = "premium_settings";
	public static final String KEY_REALTIME = "realtime";
	public static final String KEY_ACTIVE_MOD = "active_mod";
	public static final String KEY_COLLECT_STATS = "collect_stats";
	public static final String KEY_MOVE_TIMEOUT = "move_timeout";
	public static final String KEY_USE_PLAY_GAMES = "use_play_games";
	public static final String KEY_UI_ZOOM = "ui_zoom";
	public static final String KEY_VERSION_STRING = "version_string";
	public static final String KEY_TOOL_STYLE = "tool_style";
	public static final String KEY_HANDEDNESS = "handedness";
	public static final String KEY_USE_ISOMETRIC_TILES = "use_isometric_tiles";
	public static final String KEY_TILES_QUESTION_ASKED = "tiles_question_asked";

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