package com.nyrds.pixeldungeon.support;


import android.os.Build;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.AppodealNetworks;
import com.appodeal.ads.utils.Log;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.ml.EventCollector;
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

    public static double logEcpm(int adType, boolean spared) {
        if(usable() && Appodeal.isInitialized(adType)) {
            double ecpm = Appodeal.getPredictedEcpm(adType);
            EventCollector.logEvent("Appodeal_ecpm_"+adType+"_"+spared, ecpm);
            return ecpm;
        }
        return 0;
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

        String[] disableNetworks = {AppodealNetworks.AMAZON_ADS,
                AppodealNetworks.FACEBOOK,
                AppodealNetworks.FLURRY,
                AppodealNetworks.STARTAPP,
                AppodealNetworks.MOPUB,
                AppodealNetworks.MINTEGRAL,
                AppodealNetworks.OGURY_PRESAGE,
                AppodealNetworks.VUNGLE,
                AppodealNetworks.YANDEX,
                AppodealNetworks.CHARTBOOST,
                AppodealNetworks.MY_TARGET,
                AppodealNetworks.TAPJOY,
                AppodealNetworks.INMOBI,
                AppodealNetworks.SMAATO

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
