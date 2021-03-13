package com.nyrds.pixeldungeon.support;


import android.os.Build;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.utils.Log;
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.RemixedDungeon;


/**
 * Created by mike on 18.02.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class AppodealAdapter {

    public static boolean usable() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static void init() {

        if (Appodeal.isInitialized(Appodeal.BANNER)) {
            return;
        }

        final int toInitialize = Appodeal.INTERSTITIAL | Appodeal.BANNER | Appodeal.REWARDED_VIDEO;
        final int toCache = Appodeal.INTERSTITIAL | Appodeal.BANNER;

        String appKey = Game.getVar(R.string.appodealRewardAdUnitId);

        if(appKey.isEmpty()) {
            return;
        }

        Appodeal.disableLocationPermissionCheck();


        if (Util.isDebug()) {
            Appodeal.setLogLevel(Log.LogLevel.verbose);
            Appodeal.setTesting(true);
        }

        Appodeal.initialize(RemixedDungeon.instance(), appKey, toInitialize, EuConsent.getConsentLevel() == EuConsent.PERSONALIZED);
        Appodeal.cache(RemixedDungeon.instance(), toCache);
        Appodeal.setAutoCache(toCache, true);
    }
}
