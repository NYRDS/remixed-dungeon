package com.nyrds.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.nyrds.util.ModdingMode;
import com.nyrds.util.Util;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
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

        try {
            ModdingMode.selectMod(RemixedDungeon.activeMod());
            Class.forName("android.os.AsyncTask");
        } catch (Throwable ignore) {
            if(BuildConfig.DEBUG) {
                Log.d("Classes", ignore.getMessage());
            }
        }

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

    static public void startScene() {
    }

    static public Context getContext() {
        return remixedDungeonApp.getApplicationContext();
    }
}
