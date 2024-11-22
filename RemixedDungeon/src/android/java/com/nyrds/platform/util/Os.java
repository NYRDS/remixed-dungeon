package com.nyrds.platform.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;

import java.io.File;

public class Os {
    static public boolean isConnectedToInternet() {
        boolean connectionStatus;

        ConnectivityManager connectivityManager
                = (ConnectivityManager) Game.instance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        connectionStatus = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        return connectionStatus;
    }

    public static String bundle2string(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        StringBuilder string = new StringBuilder("Bundle{");
        for (String key : bundle.keySet()) {
            string.append(" ").append(key).append(" => ").append(bundle.get(key)).append(";");
        }
        string.append(" }Bundle");
        return string.toString();
    }

    @SuppressLint("NewApi")
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long ret = stat.getAvailableBytes();
        EventCollector.setSessionData("FreeInternalMemorySize", Long.toString(ret));
        return ret;
    }
}
