package com.nyrds.platform.storage;

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
	public static final String KEY_CLASSIC_TTF_FONT = "classic_ttf_font";
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
    public static final String KEY_TILES_QUESTION_ASKED = "tiles_question_asked";

	public static final String KEY_USE_SMOOTH_CAMERA = "use_smooth_camera";

	private IPreferences impl;

	public static void init(IPreferences impl) {
		INSTANCE.impl = impl;
	}

	private final Map<String, Integer> intCache    = new HashMap<>();
	private final Map<String, String>  stringCache = new HashMap<>();
	private final Map<String, Boolean> boolCache   = new HashMap<>();
	private final Map<String, Double>  doubleCache = new HashMap<>();

	public int getInt(String key, int defValue) {

		if(intCache.containsKey(key)) {
			return intCache.get(key);
		}

		String defVal = Integer.toString(defValue);
		String propVal = impl.getString(key, defVal, stringCache);
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

	public String getString(String key, String defValue) {

		if(stringCache.containsKey(key)) {
			return stringCache.get(key);
		}
		return impl.getString(key,defValue,stringCache);
	}
	public double getDouble(String key, double defValue) {

		if(doubleCache.containsKey(key)) {
			return doubleCache.get(key);
		}

		String defVal = Double.toString(defValue);
		String propVal = impl.getString(key, defVal, stringCache);

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
		
		
		
		String propVal = impl.getString(key, defVal, stringCache);
		boolean value = Boolean.parseBoolean(propVal);
		boolCache.put(key, value);
		return value;
	}

	public void put(String key, int value) {

		intCache.put(key,value);

		String val = Integer.toString(value);
		impl.put(key, val, stringCache);
	}

	public void put(String key, double value) {

		doubleCache.put(key,value);

		String val = Double.toString(value);
		impl.put(key, val, stringCache);
	}

	public void put(String key, boolean value) {

		boolCache.put(key,value);

		String val = Boolean.toString(value);
		impl.put(key, val, stringCache);
	}

	public void put(String key, String value) {
		impl.put(key, value, stringCache);
	}

}
