package com.nyrds.pixeldungeon.support;


import android.os.Build;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.AppodealNetworks;
import com.appodeal.ads.utils.Log;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.RemixedDungeon;


/**
 * Created by mike on 18.02.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class AppodealAdapter {

    public static boolean usable() {
        switch (Build.VERSION.SDK_INT) {
            case Build.VERSION_CODES.LOLLIPOP:
            case Build.VERSION_CODES.LOLLIPOP_MR1:
                return false;
            default:
                return true;
        }
    }

    public static void init() {
        final int toInitialize = Appodeal.INTERSTITIAL | Appodeal.BANNER | Appodeal.REWARDED_VIDEO;
        final int toCache = Appodeal.INTERSTITIAL | Appodeal.BANNER;

        if (Appodeal.isInitialized(Appodeal.BANNER)) {
            return;
        }

        String appKey = Game.getVar(R.string.appodealRewardAdUnitId);

        //vungle disable due to strange build issue
        //mopub, mobvista & tapjoy due audiences mismatch
        //ogury - intersiteal
        String disableNetworks[] = {AppodealNetworks.AMAZON_ADS,
                                    AppodealNetworks.ADCOLONY,
                                    AppodealNetworks.FACEBOOK,
                                    AppodealNetworks.FLURRY,
                                    AppodealNetworks.STARTAPP,
                                    AppodealNetworks.MOPUB,
                                    AppodealNetworks.MINTEGRAL,
                                    AppodealNetworks.OGURY_PRESAGE,
                                    AppodealNetworks.VUNGLE,
                                    AppodealNetworks.TAPJOY,
                                    AppodealNetworks.UNITY_ADS
        };

        for (String net : disableNetworks) {
            Appodeal.disableNetwork(RemixedDungeon.instance(), net);
        }
        Appodeal.disableLocationPermissionCheck();


        if (BuildConfig.DEBUG) {
            Appodeal.setLogLevel(Log.LogLevel.verbose);
            Appodeal.setTesting(true);
        }

        Appodeal.initialize(RemixedDungeon.instance(), appKey, toInitialize, EuConsent.getConsentLevel() == EuConsent.PERSONALIZED);
        Appodeal.cache(RemixedDungeon.instance(), toCache);
        Appodeal.setAutoCache(toCache, true);
    }
}
