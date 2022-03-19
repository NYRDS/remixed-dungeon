package com.nyrds.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.watabou.utils.Callback;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;

import lombok.SneakyThrows;

/**
 * Created by mike on 01.03.2016.
 */
public class Util {
    public static final String SAVE_ADS_EXPERIMENT = "SaveAdsExperiment2";
    public static Callback nullCallback = () -> {};

    private static String stackTraceToString(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	static public String toString(Throwable e) {
		return e.getMessage() + "\n" + Util.stackTraceToString(e) + "\n";
	}



	@SneakyThrows
	static public String getSignature(Context context) {
		PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		for (Signature signature : packageInfo.signatures) {
			md.update(signature.toByteArray());
		}
		return Base64.encodeToString(md.digest(), Base64.URL_SAFE|Base64.NO_WRAP);
	}

	public static int signum(int x) {
		return Integer.compare(x, 0);
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

	public static boolean isDebug() {
      return BuildConfig.DEBUG || GameLoop.isDev();
    }

}
