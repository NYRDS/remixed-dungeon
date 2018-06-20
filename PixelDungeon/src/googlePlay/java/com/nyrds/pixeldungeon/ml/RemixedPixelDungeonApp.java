package com.nyrds.pixeldungeon.ml;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.multidex.MultiDexApplication;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(mailTo = "nyrdsofficial@gmail.com", mode = ReportingInteractionMode.TOAST, resToastText = R.string.RemixedPixelDungeonApp_sendCrash)
public class RemixedPixelDungeonApp extends MultiDexApplication {

	@SuppressLint("StaticFieldLeak")
	static Context instanceContext;

	@Override
	public void onCreate() {
		super.onCreate();

		instanceContext = getApplicationContext();

		if(!BuildConfig.DEBUG) {
			ACRA.init(this);
		}

		try {
			Class.forName("android.os.AsyncTask");
		} catch (Throwable ignore) {
		}
	}

	static public Context getContext() {
		return instanceContext;
	}
}
