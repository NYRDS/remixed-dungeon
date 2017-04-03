package com.nyrds.pixeldungeon.ml;

import android.support.multidex.MultiDexApplication;

public class RemixedPixelDungeonApp extends MultiDexApplication {

	@Override
	public void onCreate() {
		super.onCreate();

		try {
			Class.forName("android.os.AsyncTask");
		} catch (Throwable ignore) {
		}
	}
}
