package com.nyrds.platform.util;


import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import clone.org.json.JSONArray;
import lombok.SneakyThrows;

/**
 * Created by mike on 08.03.2016.
 */
public class StringsManager {

	@NotNull
	private static final Map<Integer, String>   stringMap  = new HashMap<>();

	@NotNull
	private static final Map<Integer, String[]> stringsMap = new HashMap<>();

	private static final Map<String, String>   sStringMap  = new HashMap<>();
	private static final Map<String, String[]> sStringsMap = new HashMap<>();

	private static final Map<String, Integer> keyToInt = new HashMap<>();

    private static final Set<String> nonModdable = new HashSet<>();


    public static Set<String> missingStrings = new HashSet<>();

	static {
		addMappingForClass(R.string.class);
		addMappingForClass(R.array.class);

		nonModdable.add("easyModeAdUnitId");
		nonModdable.add("saveLoadAdUnitId");
		nonModdable.add("iapKey");
		nonModdable.add("ownSignature");
		nonModdable.add("appodealRewardAdUnitId");
		nonModdable.add("admob_publisher_id");
		nonModdable.add("admob_app_id");
		nonModdable.add("fabric_api_key");
		nonModdable.add("pollfish_key");
	}

	@SneakyThrows
	private static void addMappingForClass(@NotNull Class<?> clazz) {
		for (Field f : clazz.getDeclaredFields()) {
			if (f.isSynthetic()) {
				continue;
			}
			int key = f.getInt(null);
			String name = f.getName();

			keyToInt.put(name, key);
		}
	}

	private static void clearModStrings() {
		stringMap.clear();
		stringsMap.clear();

		sStringMap.clear();
		sStringsMap.clear();
	}

	@SneakyThrows
	private static void parseStrings(String resource) {
		File jsonFile = ModdingMode.getFile(resource);
		if (jsonFile == null || !jsonFile.exists()) {
			return;
		}

		String line = Utils.EMPTY_STRING;

		InputStream fis = new FileInputStream(jsonFile);
		InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(isr);

		while ((line = br.readLine()) != null) {
			JSONArray entry = new JSONArray(line);

			String keyString = entry.getString(0);
			Integer key = keyToInt.get(keyString);

			if (entry.length() == 2) {
				String value = entry.getString(1);

				if (key != null) {
					stringMap.put(key, value);
				}

				sStringMap.put(keyString, value);
			}

			if (entry.length() > 2) {
				String[] values = new String[entry.length() - 1];
				for (int i = 1; i < entry.length(); i++) {
					values[i - 1] = entry.getString(i);
				}

				if (key != null) {
					stringsMap.put(key, values);
				}

				sStringsMap.put(keyString, values);
			}
		}
		br.close();
	}

	private static Locale userSelectedLocale;

	private static void ensureCorrectLocale() {
		if(userSelectedLocale==null) {
			return;
		}
	}

	public static void useLocale(Locale locale, String lang) {
		userSelectedLocale = locale;

		clearModStrings();

		String modStrings = Utils.format("strings_%s.json", lang);

		if (ModdingMode.isResourceExistInMod(modStrings)) {
			parseStrings(modStrings);
		} else if (ModdingMode.isResourceExistInMod("strings_en.json")) {
			parseStrings("strings_en.json");
		}
	}

	public static String getVar(int id) {
		if (stringMap.containsKey(id)) {
			return stringMap.get(id);
		}

		ensureCorrectLocale();
		return Utils.EMPTY_STRING;
	}

	public static String @NotNull [] getVars(int id) {
		String[] baseArray = Utils.EMPTY_STRING_ARRAY;;
		String[] modStrings = Utils.EMPTY_STRING_ARRAY;

		if (stringsMap.containsKey(id)) {
			modStrings = stringsMap.get(id);
		}

		if(baseArray.length > modStrings.length) {
			ensureCorrectLocale();
			return baseArray;
		}

		return modStrings;
	}

	public static String getVar(String id) {
		if(nonModdable.contains(id)) {
			return Utils.EMPTY_STRING;
		}

		if (sStringMap.containsKey(id)) {
			return sStringMap.get(id);
		}

		if(keyToInt.containsKey(id)) {
			return getVar(keyToInt.get(id));
		}

		return Utils.EMPTY_STRING;
	}


	public static String maybeId(String maybeId, int index) {
		String[] ret = getVars(maybeId);
		if (ret.length > index) {
			missingStrings.add(maybeId);
			return ret[index];
		}
		return Utils.format("%s[%d]", maybeId, index);
	}

	public static String maybeId(String maybeId) {

		String ret = getVar(maybeId);
		if (ret.isEmpty()) {
			missingStrings.add(maybeId);
			return maybeId;
		}
		return ret;
	}

	public static String @NotNull [] getVars(String id) {
		String[] modStrings = Utils.EMPTY_STRING_ARRAY;
		String[] baseStrings = Utils.EMPTY_STRING_ARRAY;

		if (sStringsMap.containsKey(id)) {
			modStrings = sStringsMap.get(id);
		}

		if(keyToInt.containsKey(id)) {
			baseStrings =  getVars(keyToInt.get(id));
		}

		if(baseStrings.length > modStrings.length) {
			return baseStrings;
		}

		return modStrings;
	}
}
