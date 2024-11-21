package com.watabou.pixeldungeon.utils;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.util.events.EventCollector;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.Util;
import com.nyrds.util.InterstitialPoint;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.SneakyThrows;

public class HUtils {
    @NotNull
    private static final Class<?> strings = getR_Field("string");
    @NotNull
    private static final Class<?> stringArrays = getR_Field("array");

    @SneakyThrows
    static private Class<?> getR_Field(String field) {
        return Class.forName("com.nyrds.pixeldungeon.ml.R$" + field);
    }

    @SneakyThrows
    public static String[] getClassParams(String className, String paramName, String[] defaultValues, boolean warnIfAbsent) {

        if (className.isEmpty()) { // isEmpty() require api level 9
            return defaultValues;
        }

        try {
            return StringsManager.getVars(stringArrays.getField(className + "_" + paramName).getInt(null));
        } catch (NoSuchFieldException e) {
            if (warnIfAbsent) {
                GLog.w("no definition for  %s_%s :(", className, paramName);
            }
        }

        return defaultValues;
    }

    public static String getClassParam(String className, String paramName, String defaultValue, boolean warnIfAbsent) {
        if (className==null || className.isEmpty()) {
            return defaultValue;
        }

        try {
            return StringsManager.getVar(strings.getField(className + "_" + paramName).getInt(null));
        } catch (NoSuchFieldException e) {
            if (Util.isDebug() && warnIfAbsent) {
                GLog.w("no definition for  %s_%s :(", className, paramName);
            }
        } catch (Exception e) {
            EventCollector.logException(e);
        }

        return defaultValue;
    }

    public static boolean differentVersions(String v1, String v2) {
        try {
            Pattern p = Pattern.compile("\\d+(\\.\\d+)?");
            Matcher m = p.matcher(v1);
            if (m.find()) {
                v1 = m.group();
            }


            m = p.matcher(v2);
            if (m.find()) {
                v2 = m.group();
            }

            return !v1.equals(v2);

        } catch (Exception e) {
            EventCollector.logException(e);
        }
        return false;
    }

    public static String format(int StringFormatId, Object... args) {
        return String.format(Locale.ROOT, StringsManager.getVar(StringFormatId), args);
    }

    public static String indefinite(String noun) {
        //In a pt_BR language(and another), there is no specific rule.
        if (StringsManager.getVar(R.string.Utils_IsIndefinte).equals("0")) {
            return noun;
        }

        if (noun.isEmpty()) {
            return "a";
        } else {
            String VOWELS = "aoeiu";
            return (VOWELS.indexOf(Character.toLowerCase(noun.charAt(0))) != -1 ? "an " : "a ") + noun;
        }
    }

    public static class SpuriousReturn implements InterstitialPoint {

        @Override
        public void returnToWork(boolean result) {
            EventCollector.logException(new Exception(String.format("Spurious returnTo %b", result)));
        }
    }
}
