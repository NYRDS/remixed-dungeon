package com.nyrds.platform.util;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndSettings;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.input.BOMInputStream;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Port of the desktop StringsManager: parses the line-based JSON i18n bundles
 * into the id/name maps. The previous stub never loaded any strings, so every
 * getVar returned "" and all Text layers rendered empty.
 */
@LuaInterface
public class StringsManager {

    @NotNull
    private static final Map<Integer, String> stringMap = new HashMap<>();

    @NotNull
    private static final Map<Integer, String[]> stringsMap = new HashMap<>();

    private static final Map<String, String> sStringMap = new HashMap<>();
    private static final Map<String, String[]> sStringsMap = new HashMap<>();

    private static final Map<String, Integer> keyToInt = new HashMap<>();

    public static Set<String> missingStrings = new HashSet<>();

    public static final Set<Character> allChars = new HashSet<>();

    static {
        // R.names[i] is the resource name behind generated id i (make_r.py
        // emits them in id order) - reflection is not usable here.
        for (int i = 0; i < R.names.length; i++) {
            keyToInt.put(R.names[i], i);
        }
    }

    private static void clearModStrings() {
        stringMap.clear();
        stringsMap.clear();

        sStringMap.clear();
        sStringsMap.clear();
        allChars.clear();
    }

    private static void parseStrings(String resource) {
        InputStream fis = ModdingMode.getInputStream(resource);
        if (fis == null) {
            PUtil.slog("Strings", "missing strings resource: " + resource);
            return;
        }
        PUtil.slog("Strings", "Parsing Strings from " + resource);
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new BOMInputStream(fis), StandardCharsets.UTF_8));

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
                    PUtil.slog("linw", "bad json(" + e.getMessage() + ") in:" + line);
                }
            }
            try {
                br.close();
            } catch (Exception ignored) {
                // BOMInputStream.close() throws on TeaVM even after a good read
            }
        } catch (Exception e) {
            PUtil.slog("Strings", "failed to parse " + resource + ": " + e);
        }
    }

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

    public static void useLocale(Object ignoredLocale, String lang) {
        clearModStrings();

        if (!lang.equals("en")) {
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
            return ret[index];
        }
        missingStrings.add(maybeId);
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

    public static boolean isId(String id) {
        return id != null && id.startsWith(":");
    }
}
