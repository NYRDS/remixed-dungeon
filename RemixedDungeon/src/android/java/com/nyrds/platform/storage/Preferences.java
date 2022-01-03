/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.nyrds.platform.storage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.util.UserKey;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public enum Preferences {

	INSTANCE;

	public static final String KEY_LANDSCAPE        = "landscape";
	public static final String KEY_IMMERSIVE        = "immersive";
	public static final String KEY_MUSIC            = "music";
	public static final String KEY_SOUND_FX         = "soundfx";
	public static final String KEY_ZOOM             = "zoom";
	public static final String KEY_LAST_CLASS       = "last_class";
	public static final String KEY_CHALLENGES       = "challenges";
	public static final String KEY_DONATED          = "donated";
	public static final String KEY_INTRO            = "intro";
	public static final String KEY_BRIGHTNESS       = "brightness";
	public static final String KEY_LOCALE           = "locale";
	public static final String KEY_QUICKSLOTS       = "quickslots";
	public static final String KEY_VERSION          = "version";
	public static final String KEY_FONT_SCALE       = "font_scale";
	public static final String KEY_CLASSIC_FONT     = "classic_font";
	public static final String KEY_PREMIUM_SETTINGS = "premium_settings";
	public static final String KEY_REALTIME         = "realtime";
	public static final String KEY_ACTIVE_MOD       = "active_mod";
	public static final String KEY_COLLECT_STATS    = "collect_stats";
	public static final String KEY_MOVE_TIMEOUT     = "move_timeout";
	public static final String KEY_USE_PLAY_GAMES   = "use_play_games";
	public static final String KEY_PLAY_GAMES_CONNECT_FAILURES   = "play_games_connect_failures";

	public static final String KEY_UI_ZOOM        = "ui_zoom";
	public static final String KEY_VERSION_STRING = "version_string";
    public static final String KEY_TOOL_STYLE     = "tool_style";
    public static final String KEY_HANDEDNESS     = "handedness";

	public static final String KEY_EU_CONSENT_LEVEL = "eu_consent_level";
	public static final String KEY_USE_ISOMETRIC_TILES = "use_isometric_tiles";


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
