package com.nyrds.util;

import java.util.Locale;
import java.util.Random;

public class Utils {

    public static final String UNKNOWN = "unknown";
    public static final String EMPTY_STRING = "";
    public static final String[] EMPTY_STRING_ARRAY = new String[0];


    public static String capitalize(String str) {
        if(str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String format(String format, Object... args) {
        return String.format(Locale.ROOT, format, args);
    }

    public static int genderFromString(String sGender) {
        int gender = Utils.NEUTER;

        if (sGender.equals("masculine")) {
            gender = Utils.MASCULINE;
        }
        if (sGender.equals("feminine")) {
            gender = Utils.FEMININE;
        }
        return gender;
    }

    public static final int NEUTER = 0;
    public static final int MASCULINE = 1;
    public static final int FEMININE = 2;


    public static boolean canUseClassicFont(String localeCode) {
        return !(localeCode.startsWith("ko")
                || localeCode.startsWith("zh")
                || localeCode.startsWith("ja")
                || localeCode.startsWith("tr")
                || localeCode.startsWith("el"));
    }

    @SafeVarargs
    public static<T> boolean isOneOf(T value, T... array ) {
        for (T item:array) {
            if(value.equals(item)) {
                return true;
            }
        }
        return false;
    }

    public static int max(int a, int b, int c) {
        return Math.max(a,Math.max(b,c));
    }
    public static int min(int a, int b, int c) {
        return Math.min(a,Math.min(b,c));
    }


    public static float max(float a, float b, float c) {
        return Math.max(a,Math.max(b,c));
    }
    public static float min(float a, float b, float c) {
        return Math.min(a,Math.min(b,c));
    }

    public static <T extends Enum<?>> T randomEnum(Class<T> clazz){
        int x = new Random().nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

}
