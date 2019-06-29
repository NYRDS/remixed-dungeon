package com.nyrds.pixeldungeon.ml;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.watabou.pixeldungeon.Preferences;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.fabric.sdk.android.Fabric;
import io.humanteq.hqsdkapps.HqmCollectInstalledApps;
import io.humanteq.hqsdkcore.HQSdk;
import io.humanteq.hqsdkcore.api.interfaces.HqmCallback;
import io.humanteq.hqsdkcore.models.GroupResponse;

public class RemixedDungeonApp extends MultiDexApplication {

    @SuppressLint("StaticFieldLeak")
    static Context instanceContext;

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

        if(instanceContext.getResources().getString(R.string.fabric_api_key).length()>5) {
            FirebaseApp.initializeApp(this);

            Fabric.with(this, new Crashlytics());
            EventCollector.init();
        }

        if( Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            hqClear();
            if(BuildConfig.DEBUG) {
                HQSdk.enableDebug(true);
            }

            try {
                HQSdk.init(instanceContext, "22b4f34f2616d7f", true, false);
            } catch (Throwable hqSdkCrash) {
                EventCollector.logException(new HqSdkCrash(hqSdkCrash));
            }
        }

        try {
            Class.forName("android.os.AsyncTask");
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        /*
        Log.d("Callbacks",callback.getClass().getName() );
        if (!callback.getClass().getName().startsWith("com.google.android.gms.measurement.")) {
            super.registerActivityLifecycleCallbacks(callback);
        }
        */
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void hqClear() {
        if(!Preferences.INSTANCE.getBoolean("job_reset",false)) {
            HQSdk.clearJobs(getContext().getApplicationContext());
            Preferences.INSTANCE.put("job_reset",true);
        }
    }

    static public void startScene() {

        try {
            if (HQSdk.getInstance() != null) {

                HqmCollectInstalledApps.INSTANCE.start(instanceContext);
                HQSdk.startSystemEventsTracking(instanceContext);

                HQSdk.getUserGroups(new HqmCallback<List<GroupResponse>>() {

                    @Override
                    public void onSuccess(List<GroupResponse> groupResponses) {
                        if(!Preferences.INSTANCE.getBoolean("hq_groups",false)) {
                            if (!groupResponses.isEmpty()) {
                                EventCollector.logEvent("hq_non_empty_groups");
                                Preferences.INSTANCE.put("hq_groups",true);
                            } else {
                                EventCollector.logEvent("hq_empty_groups");
                            }
                        }
                    }

                    @Override
                    public void onError(@NotNull Throwable throwable) {
                        EventCollector.logException(new HqSdkError(throwable));
                    }
                });
            } else {
                EventCollector.logException(new HqSdkError(new Exception("HQM not initialized")));
            }
        } catch (Throwable hqSdkCrash) {
            EventCollector.logException(new HqSdkCrash(hqSdkCrash));
        }

    }

    static public int getExperimentSegment(String key, int vars) {

        if (!HQSdk.isInitialized()) {
            EventCollector.logEvent("experiments", key, "hqApi not ready");
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
            EventCollector.logEvent("experiments", key, "hqApi crash");
            EventCollector.logException(new HqSdkCrash(hqSdkCrash));
        }
        EventCollector.logEvent("experiments", key, Integer.toString(-1));

        return -1;
    }

    static public Context getContext() {
        return instanceContext;
    }
}
