package com.nyrds.pixeldungeon.ml;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.FirebaseApp;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.Util;
import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.RemixedDungeon;

import io.humanteq.hq_core.HQSdk;
import io.humanteq.hq_core.interfaces.HQCallback;


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
            EventCollector.init();
        }

        try {
            HQSdk.enableAttribution(false);
            HQSdk.init(getApplicationContext(), "22b4f34f2616d7f", BuildConfig.DEBUG, new HQCallback<Void>(){

                @Override
                public void onSuccess(Void aVoid) {

                }

                @Override
                public void onError(Throwable throwable) {
                    EventCollector.logException(new HqSdkError(throwable));
                }
            });
        } catch (Throwable hqSdkCrash) {
            EventCollector.logException(new HqSdkCrash(hqSdkCrash));
        }


        try {
            ModdingMode.selectMod(RemixedDungeon.activeMod());
            Class.forName("android.os.AsyncTask");
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

    static public void startScene() {

        try {
            HQSdk.start(getContext());
        } catch (Throwable hqSdkCrash) {
            EventCollector.logException(new HqSdkCrash(hqSdkCrash));
        }

    }

    static public Context getContext() {
        return remixedDungeonApp.getApplicationContext();
    }
}
