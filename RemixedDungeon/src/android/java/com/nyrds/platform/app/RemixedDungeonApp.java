package com.nyrds.platform.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.nyrds.market.MarketApp;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Util;

import java.security.MessageDigest;
import java.util.concurrent.ThreadPoolExecutor;

import lombok.SneakyThrows;

public class RemixedDungeonApp extends MultiDexApplication {

    @SuppressLint("StaticFieldLeak")
    static RemixedDungeonApp remixedDungeonApp;

    @Override
    public void onCreate() {
        super.onCreate();

        remixedDungeonApp = this;

        registerActivityLifecycleCallbacks(new RemixedActivityLifecycleCallbacks());

        if(AsyncTask.THREAD_POOL_EXECUTOR instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor defaultExecutor = (ThreadPoolExecutor) AsyncTask.THREAD_POOL_EXECUTOR;
            defaultExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        }

        if(checkOwnSignature()) {
            MarketApp.init(this);
        }

        try {
            ModdingMode.selectMod(GamePreferences.activeMod());
            Class.forName("android.os.AsyncTask");
        } catch (Throwable ignore) {
            if(Util.isDebug()) {
                Log.d("Classes", ignore.toString());
            }
        }
/*
            WebServer server = new WebServer(8080);
            try {
                server.start();
            } catch (IOException e) {
                EventCollector.logException(e,"WebServer");
            }
            try {
                for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                    Enumeration<InetAddress> addrs = networkInterface.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        Log.d("IP", addrs.nextElement().getHostAddress());
                    }
                }
            } catch (SocketException e) {
                EventCollector.logException(e,"IP");
            }
*/
    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        if(Util.isDebug()) {
            Log.d("Callbacks", callback.getClass().getName());
        }
//        if (!callback.getClass().getName().startsWith("com.google.android.gms.measurement.")) {
            super.registerActivityLifecycleCallbacks(callback);
//        }
    }

    @SneakyThrows
    static public boolean checkOwnSignature() {
        //Log.i("Game", Utils.format("own signature %s", Util.getSignature(this)));
        if(Util.isDebug()) {
            return true;
        }

        Context context = getContext();
        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        for (Signature signature : packageInfo.signatures) {
            md.update(signature.toByteArray());
        }
        return Base64.encodeToString(md.digest(), Base64.URL_SAFE | Base64.NO_WRAP).equals(getContext().getResources().getString(R.string.ownSignature));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    static public Context getContext() {
        return remixedDungeonApp.getApplicationContext();
    }

    private class RemixedActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            remixedDungeonApp = RemixedDungeonApp.this;
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            remixedDungeonApp = RemixedDungeonApp.this;
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            remixedDungeonApp = RemixedDungeonApp.this;
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }
    }
}
