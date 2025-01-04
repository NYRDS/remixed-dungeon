package com.nyrds.util;

import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.platform.game.RemixedDungeon;
import com.watabou.utils.Callback;

import org.hjson.JsonValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

import lombok.SneakyThrows;

/**
 * Created by mike on 01.03.2016.
 */
public class Util {
    public static final String SAVE_ADS_EXPERIMENT = "SaveAdsExperiment2";
    public static final Callback nullCallback = () -> {};
	public static final float BIG_FLOAT = Float.MAX_VALUE / 16384;

    private static String stackTraceToString(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	static public String toString(Throwable e) {
		return e.getMessage() + "\n" + Util.stackTraceToString(e) + "\n";
	}

	public static int signum(int x) {
		return Integer.compare(x, 0);
	}

	public static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	public static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}

	@Nullable
	@SneakyThrows
	public static <T> T byNameFromList(Class<?>[] classList, @NotNull String name) {
		for (Class<?> clazz : classList) {
			if (clazz.getSimpleName().equals(name)) {
				return (T) clazz.newInstance();
			}
		}
		return null;
	}

	static public  int indexOf(Class<?>[] classList, @NotNull String name) {
		int index = 0;
		for (Class<?> clazz : classList) {
			if (clazz.getSimpleName().equals(name)) {
				return index;
			}
			++index;
		}
		return -1;
	}

	@SneakyThrows
	public static JSONObject sanitizeJson(String jsonInput) {
		JsonValue jsonValue = JsonValue.readHjson(jsonInput).asObject();
        return new JSONObject(jsonValue.toString());
	}

	@SneakyThrows
	public static JSONArray sanitizeJsonArray(String jsonInput) {
		JsonValue jsonValue = JsonValue.readHjson(jsonInput).asArray();
		return new JSONArray(jsonValue.toString());
	}

	public static boolean isDebug() {
      return BuildConfig.DEBUG || RemixedDungeon.isDev();
    }

}
