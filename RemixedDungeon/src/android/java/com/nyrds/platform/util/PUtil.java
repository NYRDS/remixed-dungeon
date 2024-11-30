package com.nyrds.platform.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.Preferences;

import java.io.File;
import java.util.UUID;

public class PUtil {
    static public boolean isConnectedToInternet() {
        boolean connectionStatus;

        ConnectivityManager connectivityManager
                = (ConnectivityManager) Game.instance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        connectionStatus = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        return connectionStatus;
    }

    static public void slog(String tag, String txt) {
        Log.i(tag, txt);
    }

    public static UUID getUserId() {
        final String noKey="noKey";

        SharedPreferences prefs = Preferences.INSTANCE.get();

        UUID userId = UUID.randomUUID();
        String key = prefs.getString("userKey", noKey);
        if(key.equals(noKey)) {
            prefs.edit().putString("userKey", userId.toString()).apply();
        } else {
            userId = UUID.fromString(key);
        }
        return userId;
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
        long ret;
        if (android.os.Build.VERSION.SDK_INT < 18) {
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            ret = availableBlocks * blockSize;
        } else {
            ret = stat.getAvailableBytes();
        }
        EventCollector.setSessionData("FreeInternalMemorySize", Long.toString(ret));
        return ret;
    }
}
