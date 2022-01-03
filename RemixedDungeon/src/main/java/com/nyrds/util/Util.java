package com.nyrds.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Base64;

import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.watabou.utils.Callback;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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

	static public boolean isConnectedToInternet() {
		boolean connectionStatus;

		ConnectivityManager connectivityManager
				= (ConnectivityManager) Game.instance().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		connectionStatus = activeNetworkInfo != null && activeNetworkInfo.isConnected();
		return connectionStatus;
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

    public static String bundle2string(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        StringBuilder string = new StringBuilder("Bundle{");
        for (String key : bundle.keySet()) {
            string.append(" ").append(key).append(" => ").append(bundle.get(key)).append(";");
        }
        string.append(" }Bundle");
        return string.toString();
    }

    @SuppressLint("NewApi")
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long ret;
        if (android.os.Build.VERSION.SDK_INT < 18) {
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            ret = availableBlocks * blockSize;
        } else {
            ret = stat.getAvailableBytes();
        }
        EventCollector.setSessionData("FreeInternalMemorySize", Long.toString(ret));
        return ret;
    }

    public static boolean isDebug() {
      return BuildConfig.DEBUG || RemixedDungeon.isDev();
    }

}
