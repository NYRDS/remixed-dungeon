package com.nyrds.pixeldungeon.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;


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
    }
}
