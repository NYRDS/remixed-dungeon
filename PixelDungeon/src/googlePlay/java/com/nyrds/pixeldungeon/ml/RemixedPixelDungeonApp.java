package com.nyrds.pixeldungeon.ml;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class RemixedPixelDungeonApp extends MultiDexApplication {

	@SuppressLint("StaticFieldLeak")
	static Context instanceContext;

	@Override
	public void onCreate() {
		super.onCreate();

		instanceContext = getApplicationContext();

		Fabric.with(this, new Crashlytics());

		try {
			Class.forName("android.os.AsyncTask");
		} catch (Throwable ignore) {
		}
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	static public Context getContext() {
		return instanceContext;
	}
}
