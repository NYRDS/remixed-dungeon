package com.nyrds.pixeldungeon.ml;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class RemixedPixelDungeonApp extends Application {

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

	static public Context getContext() {
		return instanceContext;
	}
}
