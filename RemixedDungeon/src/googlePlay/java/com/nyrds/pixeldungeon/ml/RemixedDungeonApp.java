package com.nyrds.pixeldungeon.ml;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import io.fabric.sdk.android.Fabric;

public class RemixedDungeonApp extends MultiDexApplication {

	@SuppressLint("StaticFieldLeak")
	static Context instanceContext;

	@Override
	public void onCreate() {
		super.onCreate();

		instanceContext = getApplicationContext();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			FirebaseApp.initializeApp(this);
		}

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
