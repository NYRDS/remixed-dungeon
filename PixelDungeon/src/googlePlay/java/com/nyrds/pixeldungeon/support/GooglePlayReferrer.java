package com.nyrds.pixeldungeon.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.appbrain.ReferrerReceiver;
import com.google.android.gms.analytics.AnalyticsReceiver;
import com.yandex.metrica.MetricaEventHandler;


public class GooglePlayReferrer extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        if (extras != null) {
            String referrerString = extras.getString("referrer");
            if (referrerString != null) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putString("referrer", referrerString);
                edit.apply();
            }
        }

        ReferrerReceiver appBrainReceiver = new ReferrerReceiver();
        appBrainReceiver.onReceive(context, intent);

        com.yandex.metrica.MetricaEventHandler yandexReferrer = new MetricaEventHandler();
        yandexReferrer.onReceive(context, intent);

        AnalyticsReceiver receiver = new AnalyticsReceiver();
        receiver.onReceive(context, intent);
    }
}
