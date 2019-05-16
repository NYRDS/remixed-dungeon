package com.nyrds.pixeldungeon.ml;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.fabric.sdk.android.Fabric;
import io.humanteq.hqsdkapps.InstalledApplicationsCollector;
import io.humanteq.hqsdkcore.HQSdk;
import io.humanteq.hqsdkcore.api.interfaces.HqmCallback;

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
        FirebaseApp.initializeApp(this);

        Fabric.with(this, new Crashlytics());
        EventCollector.init();

        if( Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            //HQSdk.enableDebug(true);

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
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    static public void startScene() {

        try {
            if (HQSdk.getInstance() != null) {

                HQSdk.start(new InstalledApplicationsCollector());
                HQSdk.startSystemEventsTracking();

                HQSdk.getUserGroups(new HqmCallback<List<String>>() {

                    @Override
                    public void onSuccess(List<String> strings) {
                        for(String group:strings) {
                            GLog.debug("HQ:"+group);
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
