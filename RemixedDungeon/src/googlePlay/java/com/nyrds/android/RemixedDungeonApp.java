package com.nyrds.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.FirebaseApp;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.RemixedDungeon;

import java.util.concurrent.ThreadPoolExecutor;

public class RemixedDungeonApp extends MultiDexApplication {

    @SuppressLint("StaticFieldLeak")
    static RemixedDungeonApp remixedDungeonApp;

    @Override
    public void onCreate() {
        super.onCreate();

        remixedDungeonApp = this;

        if(AsyncTask.THREAD_POOL_EXECUTOR instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor defaultExecutor = (ThreadPoolExecutor) AsyncTask.THREAD_POOL_EXECUTOR;
            defaultExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        }

        if(checkOwnSignature()) {
            FirebaseApp.initializeApp(this);
            EventCollector.init();
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
        if(BuildConfig.DEBUG) {
            return true;
        };
        return Util.getSignature(getContext()).equals(StringsManager.getVar(R.string.ownSignature));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    static public Context getContext() {
        return remixedDungeonApp.getApplicationContext();
    }
}
