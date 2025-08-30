package com.nyrds.platform.util;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.utils.Utils;

import org.apache.commons.io.input.BOMInputStream;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * HTML version of StringsManager
 */
public class StringsManager {
    
    private static final Map<Integer, String> stringMap = new HashMap<>();
    private static final Map<Integer, String[]> stringsMap = new HashMap<>();
    private static final Map<String, String> sStringMap = new HashMap<>();
    private static final Map<String, String[]> sStringsMap = new HashMap<>();
    private static final Map<String, Integer> keyToInt = new HashMap<>();
    private static final Set<String> nonModdable = new HashSet<>();
    
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
    
    private static void addMappingForClass(Class<?> clazz) {
        try {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isSynthetic()) {
                    continue;
                }
                int key = f.getInt(null);
                String name = f.getName();
                
                keyToInt.put(name, key);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    public static String getVar(int id) {
        if (stringMap.containsKey(id)) {
            return stringMap.get(id);
        }
        
        return Utils.EMPTY_STRING;
    }
    
    public static String[] getVars(int id) {
        String[] baseArray = Utils.EMPTY_STRING_ARRAY;
        String[] modStrings = Utils.EMPTY_STRING_ARRAY;
        
        if (stringsMap.containsKey(id)) {
            modStrings = stringsMap.get(id);
        }
        
        if (baseArray.length > modStrings.length) {
            return baseArray;
        }
        
        return modStrings;
    }
    
    public static String getVar(String id) {
        if (nonModdable.contains(id)) {
            return Utils.EMPTY_STRING;
        }
        
        if (sStringMap.containsKey(id)) {
            return sStringMap.get(id);
        }
        
        if (keyToInt.containsKey(id)) {
            return getVar(keyToInt.get(id));
        }
        
        return Utils.EMPTY_STRING;
    }
    
    public static String getVar(String id, Object... args) {
        return String.format(getVar(id), args);
    }
    
    public static String maybeId(String id) {
        String ret = getVar(id);
        if (ret.isEmpty()) {
            return id;
        }
        return ret;
    }
    
    public static String maybeId(String id, int index) {
        String[] ret = getVars(id);
        if (ret.length > index) {
            return ret[index];
        }
        return Utils.format("%s[%d]", id, index);
    }
    
    public static String[] getVars(String id) {
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
    
    public static void useLocale(Object locale, String lang) {
        // Simple implementation for HTML version
        // Locale handling is limited in HTML environment
    }
    
    public static String[] getStringsArray(String id) {
        return getVars(id);
    }
    
    public static String[] getStringsArray(String id, Object... args) {
        String[] strings = getVars(id);
        String[] formatted = new String[strings.length];
        for (int i = 0; i < strings.length; i++) {
            formatted[i] = String.format(strings[i], args);
        }
        return formatted;
    }
}