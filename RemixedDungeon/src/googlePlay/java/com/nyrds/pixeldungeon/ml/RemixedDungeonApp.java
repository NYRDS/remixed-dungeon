package com.nyrds.pixeldungeon.ml;

import android.annotation.SuppressLint;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.watabou.noosa.Game;

import org.jetbrains.annotations.NotNull;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import io.fabric.sdk.android.Fabric;
import io.humanteq.hqsdkapps.InstalledApplicationsCollector;
import io.humanteq.hqsdkcore.HQSdk;
import io.humanteq.hqsdkcore.api.impl.HqmApi;
import io.humanteq.hqsdkcore.api.interfaces.HqmCallback;

public class RemixedDungeonApp extends MultiDexApplication {

	@SuppressLint("StaticFieldLeak")
	static Context instanceContext;

	private static boolean hqmStarted = false;

	@Override
	public void onCreate() {
		super.onCreate();

		instanceContext = getApplicationContext();
		FirebaseApp.initializeApp(this);

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

	static public void startScene() {
		if(hqmStarted) {
			return;
		}

		HQSdk.init(Game.instance(), "22b4f34f2616d7f", true, false,
				new HqmCallback<HqmApi>() {

					@Override
					public void onSuccess(HqmApi hqmApi) {
						hqmStarted = true;
						hqmApi.start(new InstalledApplicationsCollector());
						hqmApi.startSystemEventsTracking();
					}

					@Override
					public void onError(@NotNull Throwable throwable) {
						EventCollector.logException(throwable, "hq sdk init error");
					}
				}

		);
	}

	static public Context getContext() {
		return instanceContext;
	}
}
