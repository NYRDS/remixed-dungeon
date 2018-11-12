package com.nyrds.pixeldungeon.ml;

import android.annotation.SuppressLint;
import android.content.Context;

import com.crashlytics.android.Crashlytics;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import io.fabric.sdk.android.Fabric;
import ru.livli.swsdk.SWSdk;

public class RemixedPixelDungeonApp extends MultiDexApplication {

	@SuppressLint("StaticFieldLeak")
	static Context instanceContext;

	@Override
	public void onCreate() {
		super.onCreate();

		instanceContext = getApplicationContext();

		Fabric.with(this, new Crashlytics());

		SWSdk.Companion.getInstance(this, "22b4f34f2616d7f");

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
