package com.nyrds.pixeldungeon.ml;

import android.annotation.SuppressLint;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	private static int hqSdkStarted = 0;

	@Nullable
	private static HqmApi hqApi;

	@Override
	public void onCreate() {
		super.onCreate();

		instanceContext = getApplicationContext();
		FirebaseApp.initializeApp(this);

		Fabric.with(this, new Crashlytics());

		HQSdk.init(instanceContext, "22b4f34f2616d7f", true, false,
				new HqmCallback<HqmApi>() {

					@Override
					public void onSuccess(HqmApi hqmApi) {
						hqApi = hqmApi;
						hqSdkStarted = 1;
					}

					@Override
					public void onError(@NotNull Throwable throwable) {
						EventCollector.logException(throwable, "hq sdk init error");
					}
				}

		);


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
		if(hqSdkStarted == 1 && hqApi != null) {
			hqApi.start(new InstalledApplicationsCollector());
			hqApi.startSystemEventsTracking();
			hqSdkStarted++;
		}
	}

	static public int getExperimentSegment(String key, int vars) {
		if(hqApi==null) {
			return -1;
		}

		return HQSdk.getTestGroup(key,vars).intValue();
	}

	static public Context getContext() {
		return instanceContext;
	}
}
