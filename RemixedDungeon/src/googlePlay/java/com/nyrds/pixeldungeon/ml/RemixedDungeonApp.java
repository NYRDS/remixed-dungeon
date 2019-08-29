package com.nyrds.pixeldungeon.ml;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.Util;
import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.Preferences;
import com.watabou.pixeldungeon.RemixedDungeon;

import io.fabric.sdk.android.Fabric;
import io.humanteq.hqsdkapps.HqmCollectInstalledApps;
import io.humanteq.hqsdkcore.HQSdk;

public class RemixedDungeonApp extends MultiDexApplication {

    @SuppressLint("StaticFieldLeak")
    static RemixedDungeonApp remixedDungeonApp;

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

        remixedDungeonApp = this;

        if(checkOwnSignature()) {
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
                HQSdk.init(getApplicationContext(), "22b4f34f2616d7f", true, false);
            } catch (Throwable hqSdkCrash) {
                EventCollector.logException(new HqSdkCrash(hqSdkCrash));
            }
        }

        try {
            ModdingMode.selectMod(RemixedDungeon.activeMod());
            Class.forName("android.os.AsyncTask");
            Class.forName("com.nyrds.pixeldungeon.mechanics.spells.SpellFactory");
        } catch (Throwable ignore) {
            if(BuildConfig.DEBUG) {
                Log.d("Classes", ignore.getMessage());
            }
        }
    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        if(BuildConfig.DEBUG) {
            Log.d("Callbacks", callback.getClass().getName());
        }
//        if (!callback.getClass().getName().startsWith("com.google.android.gms.measurement.")) {
            super.registerActivityLifecycleCallbacks(callback);
//        }
    }

    static public boolean checkOwnSignature() {
        //Log.i("Game", Utils.format("own signature %s", Util.getSignature(this)));
        return Util.getSignature(getContext()).equals(StringsManager.getVar(R.string.ownSignature));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void hqClear() {
        if(!Preferences.INSTANCE.getBoolean("job_reset",false)) {
            Preferences.INSTANCE.put("job_reset",true);
        }
    }

    static public void startScene() {

        try {
            if (HQSdk.getInstance() != null) {
                HqmCollectInstalledApps.INSTANCE.start(getContext());
                HQSdk.startSystemEventsTracking(getContext());
            } else {
                EventCollector.logException(new HqSdkError(new Exception("HQM not initialized")));
            }
        } catch (Throwable hqSdkCrash) {
            EventCollector.logException(new HqSdkCrash(hqSdkCrash));
        }

    }

    static public Context getContext() {
        return remixedDungeonApp.getApplicationContext();
    }
}
