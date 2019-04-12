package com.nyrds.pixeldungeon.ml;

import android.annotation.SuppressLint;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

	static class HqSdkCrash extends Exception {
		HqSdkCrash(Throwable cause) {
			super(cause);
		}
	}

	static class HqSdkError extends Exception {
		HqSdkError(Throwable cause) {
			super(cause);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		instanceContext = getApplicationContext();
		FirebaseApp.initializeApp(this);

		Fabric.with(this, new Crashlytics());
		EventCollector.init();

		//HQSdk.enableDebug(true);

		try {
			HQSdk.init(instanceContext, "22b4f34f2616d7f", true, false,
					new HqmCallback<HqmApi>() {

						@Override
						public void onSuccess(HqmApi hqmApi) {
							hqApi = hqmApi;
							hqSdkStarted = 1;
						}

						@Override
						public void onError(@NotNull Throwable throwable) {
							EventCollector.logException(new HqSdkError(throwable));
						}
					}

			);
		} catch (Throwable hqSdkCrash) {
			EventCollector.logException(new HqSdkCrash(hqSdkCrash));
		}


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
			try {
				hqApi.start(new InstalledApplicationsCollector());
				hqApi.startSystemEventsTracking();
				hqSdkStarted++;

				HQSdk.getUserGroups(new HqmCallback<List<String>>() {

					@Override
					public void onError(@NotNull Throwable throwable) {
						EventCollector.logException(new HqSdkError(throwable));
					}

					@Override
					public void onSuccess(List<String> list) {
						//TODO do something here
					}
				});
			} catch (Throwable hqSdkCrash) {
				EventCollector.logException(new HqSdkCrash(hqSdkCrash));
			}
		}
	}

	static public int getExperimentSegment(String key, int vars) {
		if(hqApi==null) {
			EventCollector.logEvent("experiments",key,"hqApi not ready");
			return -1;
		}

		try {
			Long hqsdkRet = HQSdk.getTestGroup(key, vars);
			if (hqsdkRet != null) {
				int groupId = hqsdkRet.intValue();
				EventCollector.logEvent("experiments", key, Integer.toString(groupId));
				return groupId;
			}
		} catch (Throwable hqSdkCrash) {
			EventCollector.logEvent("experiments",key,"hqApi crash");
			EventCollector.logException(new HqSdkCrash(hqSdkCrash));
		}
		EventCollector.logEvent("experiments", key, Integer.toString(-1));
		return -1;
	}

	static public Context getContext() {
		return instanceContext;
	}
}
