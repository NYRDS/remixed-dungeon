package com.watabou.noosa;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by mike on 08.03.2016.
 */
public class StringsManager {
	private static Context context;

	@SuppressLint("UseSparseArrays")
	@NonNull
	private static Map<Integer, String>   stringMap  = new HashMap<>();
	@SuppressLint("UseSparseArrays")
	@NonNull
	private static Map<Integer, String[]> stringsMap = new HashMap<>();

	private static Map<String, String>   sStringMap  = new HashMap<>();
	private static Map<String, String[]> sStringsMap = new HashMap<>();

	private static Map<String, Integer> keyToInt;

	private static void addMappingForClass(Class<?> clazz) {
		for (Field f : clazz.getDeclaredFields()) {
			if (f.isSynthetic()) {
				continue;
			}
			int key;
			try {
				key = f.getInt(null);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				throw new TrackedRuntimeException(e);
			}
			String name = f.getName();

			keyToInt.put(name, key);
		}
	}

	private static void initTextMapping() {
		long mapStart = System.nanoTime();

		keyToInt = new HashMap<>();

		addMappingForClass(R.string.class);
		addMappingForClass(R.array.class);

		long mapEnd = System.nanoTime();
		GLog.toFile("map creating time %f", (mapEnd - mapStart) / 1000000f);
	}

	private static void parseStrings(String resource) {
		File jsonFile = ModdingMode.getFile(resource);
		if (jsonFile == null) {
			return;
		}

		if (!jsonFile.exists()) {
			return;
		}

		if (keyToInt == null) {
			initTextMapping();
		}

		stringMap.clear();
		stringsMap.clear();

		sStringMap.clear();
		sStringsMap.clear();

		String line = "";

		try {
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
		} catch (IOException e) {
			throw new TrackedRuntimeException(e);
		} catch (JSONException e) {
			Game.toast("malformed json: [%s] in [%s] ignored ", line, resource);
		}
	}

	public static void useLocale(Locale locale, String lang) {
		Configuration config = context.getResources().getConfiguration();
		config.locale = locale;
		context.getResources().updateConfiguration(config,
				context.getResources().getDisplayMetrics());

		String modStrings = Utils.format("strings_%s.json", lang);

		if (ModdingMode.isResourceExistInMod(modStrings)) {
			parseStrings(modStrings);
		} else if (ModdingMode.isResourceExistInMod("strings_en.json")) {
			parseStrings("strings_en.json");
		}

	}

	public static void setContext(Context context) {
		StringsManager.context = context;
	}

	public static String getVar(int id) {
		if (id != R.string.easyModeAdUnitId && id != R.string.saveLoadAdUnitId
				&& id != R.string.easyModeSmallScreenAdUnitId && id != R.string.iapKey
				&& id != R.string.testDevice && id != R.string.ownSignature) {
			if (stringMap.containsKey(id)) {
				return stringMap.get(id);
			}
		}

		try {
			return context.getResources().getString(id);
		} catch (Resources.NotFoundException notFound) {
			GLog.w("resource not found: %s", notFound.getMessage());
		}
		return "";
	}

	public static String[] getVars(int id) {

		if (stringsMap.containsKey(id)) {
			return stringsMap.get(id);
		}

		return context.getResources().getStringArray(id);
	}

	public static String getVar(String id) {
		if (id.equals("easyModeAdUnitId") || id.equals("saveLoadAdUnitId")
				|| id.equals("easyModeSmallScreenAdUnitId") || id.equals("iapKey")
				|| id.equals("testDevice") || id.equals("ownSignature")) {
			return "";
		}

		if (sStringMap.containsKey(id)) {
			return sStringMap.get(id);
		}

		return "";
	}

	public static String maybeId(String maybeId) {

		String ret = getVar(maybeId);
		if (ret.isEmpty()) {
			return maybeId;
		}
		return ret;
	}

	public static String[] getVars(String id) {
		if (sStringsMap.containsKey(id)) {
			return sStringsMap.get(id);
		}

		return new String[1];
	}

}
