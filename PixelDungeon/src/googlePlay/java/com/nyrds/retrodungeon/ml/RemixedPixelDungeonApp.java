package com.nyrds.retrodungeon.ml;

import android.support.multidex.MultiDexApplication;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(mailTo = "nyrdsofficial@gmail.com", mode = ReportingInteractionMode.TOAST, resToastText = R.string.RemixedPixelDungeonApp_sendCrash)
public class RemixedPixelDungeonApp extends MultiDexApplication {

	@Override
	public void onCreate() {
		super.onCreate();
		if(!BuildConfig.DEBUG) {
			ACRA.init(this);
		}

		try {
			Class.forName("android.os.AsyncTask");
		} catch (Throwable ignore) {
		}
	}
}
