package com.nyrds.platform.util;


import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndSettings;

import org.apache.commons.io.input.BOMInputStream;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lombok.SneakyThrows;

/**
 * Created by mike on 08.03.2016.
 */
public class StringsManager {

    @NotNull
    private static final Map<Integer, String> stringMap = new HashMap<>();

    @NotNull
    private static final Map<Integer, String[]> stringsMap = new HashMap<>();

    private static final Map<String, String> sStringMap = new HashMap<>();
    private static final Map<String, String[]> sStringsMap = new HashMap<>();

    private static final Map<String, Integer> keyToInt = new HashMap<>();

    public static Set<String> missingStrings = new HashSet<>();

    static {
        addMappingForClass(R.string.class);
        addMappingForClass(R.array.class);
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
        allChars.clear();
    }

    @SneakyThrows
    private static void parseStrings(String resource) {
        PUtil.slog("Strings", "Parsing Strings from " + resource);
        InputStream fis = ModdingMode.getInputMergedInputStream(resource);
        InputStreamReader isr = new InputStreamReader(new BOMInputStream(fis), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);

        String line;

        while ((line = br.readLine()) != null) {
            try {
                JSONArray entry = new JSONArray(line);

                String keyString = entry.getString(0);
                Integer key = keyToInt.get(keyString);

                if (entry.length() == 2) {
                    String value = entry.getString(1);

                    if (key != null) {
                        stringMap.put(key, value);
                    }

                    sStringMap.put(keyString, value);

                    for (char c : value.toCharArray()) {
                        allChars.add(c);
                    }
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

                    for (String s : values) {
                        for (char c : s.toCharArray()) {
                            allChars.add(c);
                        }
                    }
                }
            } catch (JSONException e) {

                PUtil.slog("linw", "bad json("+ e.getMessage() + ") in:" + line);
            }
        }
        br.close();
    }

    public static final Set<Character> allChars = new HashSet<>();

    public static String getAllCharsAsString() {
        for (String lang : WndSettings.langNames) {
            for (char c : lang.toCharArray()) {
                allChars.add(c);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Character c : allChars) {
            sb.append(c);
        }

        return sb.toString();
    }


    public static void useLocale(Locale ignoredLocale, String lang) {
        clearModStrings();

        if(!lang.equals("en")) {
            parseStrings("strings_en.json");
        }

        String modStrings = Utils.format("strings_%s.json", lang);

        parseStrings(modStrings);
    }

    public static String getVar(int id) {
        if (stringMap.containsKey(id)) {
            return stringMap.get(id).replaceAll("(?<!\")(%(\\d+\\$)?[-+ 0-9.,#()]*[doxXfFeEgGaA])", "\"$1\"");
        }

        return Utils.EMPTY_STRING;
    }

    public static String @NotNull [] getVars(int id) {
        String[] baseArray = Utils.EMPTY_STRING_ARRAY;
        String[] modStrings = Utils.EMPTY_STRING_ARRAY;

        if (stringsMap.containsKey(id)) {
            modStrings = stringsMap.get(id);
        }

        if (baseArray.length > modStrings.length) {
            return baseArray;
        }

        String[] result = modStrings.clone();
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i].replaceAll("(?<!\")(%(\\d+\\$)?[-+ 0-9.,#()]*[doxXfFeEgGaA])", "\"$1\"");
        }
        return result;
    }

    public static String getVar(String id) {
        if (sStringMap.containsKey(id)) {
            return sStringMap.get(id).replaceAll("(?<!\")(%(\\d+\\$)?[-+ 0-9.,#()]*[doxXfFeEgGaA])", "\"$1\"");
        }

        if (keyToInt.containsKey(id)) {
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

        if (keyToInt.containsKey(id)) {
            baseStrings = getVars(keyToInt.get(id));
        }

        if (baseStrings.length > modStrings.length) {
            return baseStrings;
        }

        return modStrings;
    }
}
