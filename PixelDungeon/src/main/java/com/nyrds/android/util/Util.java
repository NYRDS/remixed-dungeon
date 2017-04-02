package com.nyrds.android.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.nyrds.pixeldungeon.ml.EventCollector;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by mike on 01.03.2016.
 */
public class Util {
	private static String stackTraceToString(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	static public String toString(Exception e) {
		return e.getMessage() + "\n" + Util.stackTraceToString(e) + "\n";
	}

	static public void storeEventInAcra(String eventKey, Exception e) {
		EventCollector.logException(e,eventKey);
//		if (!ACRA.isInitialised()) {
//			return;
//		}
//		ACRA.getErrorReporter().putCustomData(eventKey, toString(e));
	}

	static public void storeEventInAcra(String eventKey, String str) {
//		if (!ACRA.isInitialised()) {
//			return;
//		}
//		ACRA.getErrorReporter().putCustomData(eventKey, str);
	}

	static public boolean isConnectedToInternet() {
		InetAddress ipAddr;
		try {
			ipAddr = InetAddress.getByName("google.com");
		} catch (UnknownHostException e) {
			return false;
		}

		return !ipAddr.toString().equals("");
	}

	static public String getSignature(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			for (Signature signature : packageInfo.signatures) {
				md.update(signature.toByteArray());
			}
			return Base64.encodeToString(md.digest(), Base64.URL_SAFE);
		} catch (PackageManager.NameNotFoundException e) {
			throw new TrackedRuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			return "No SHA-1?";
		}
	}

	public static int signum(int x) {
		if(x>0) {
			return 1;
		}
		if(x<0) {
			return -1;
		}
		return 0;
	}

	@Nullable
	public static<T> T byNameFromList(Class<?>[] classList, @NonNull String name) {
		for(Class<?> clazz:classList) {
			if (clazz.getSimpleName().equals(name)) {
				try {
					return (T) clazz.newInstance();
				} catch (Exception e) {
					throw new TrackedRuntimeException(e);
				}
			}
		}
		return null;
	}
}
