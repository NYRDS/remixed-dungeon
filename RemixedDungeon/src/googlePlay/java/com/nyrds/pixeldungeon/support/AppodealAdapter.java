package com.nyrds.pixeldungeon.support;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.RewardedVideoCallbacks;
import com.appodeal.ads.utils.Log;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.RemixedDungeon;


/**
 * Created by mike on 18.02.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class AppodealAdapter {

    private static final String APPODEAL_REWARD_VIDEO = "appodeal reward video";
    private static InterstitialPoint returnTo;

    public static void init() {

        final int toInitialize = Appodeal.INTERSTITIAL | Appodeal.BANNER;
        final int toCache = Appodeal.INTERSTITIAL | Appodeal.BANNER;

        if (Appodeal.isInitialized(Appodeal.BANNER)) {
            return;
        }

        String appKey = Game.getVar(R.string.appodealRewardAdUnitId);

        //vungle disable due to strange build issue
        //mopub, mobvista & tapjoy due audiences mismatch
        //ogury - intersiteal
        String disableNetworks[] = {"adcolony","facebook", "flurry", "startapp", "vungle", "mopub", "mobvista", "tapjoy", "ogury"};

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


    public static void initRewardedVideo() {

        Game.instance().runOnUiThread(() -> {
            init();

            EventCollector.startTrace(APPODEAL_REWARD_VIDEO);

            Appodeal.cache(RemixedDungeon.instance(), Appodeal.REWARDED_VIDEO);
            Appodeal.setAutoCache(Appodeal.REWARDED_VIDEO, true);

            Appodeal.setRewardedVideoCallbacks(new RewardedVideoCallbacks() {

                @Override
                public void onRewardedVideoLoaded(boolean b) {
                    EventCollector.stopTrace(APPODEAL_REWARD_VIDEO, APPODEAL_REWARD_VIDEO, "ok", "");
                }

                @Override
                public void onRewardedVideoFailedToLoad() {
                    EventCollector.stopTrace(APPODEAL_REWARD_VIDEO, APPODEAL_REWARD_VIDEO, "fail", "");
                }

                @Override
                public void onRewardedVideoShown() {
                }

                @Override
                public void onRewardedVideoFinished(double v, String s) {

                }

                @Override
                public void onRewardedVideoClosed(final boolean finished) {
                    returnTo.returnToWork(finished);
                }

                @Override
                public void onRewardedVideoExpired() {

                }
            });
        });
    }

    public static void showCinemaRewardVideo(InterstitialPoint ret) {
        returnTo = ret;
        Game.instance().runOnUiThread(() -> {
            if (isVideoReady()) {
                Appodeal.show(RemixedDungeon.instance(), Appodeal.REWARDED_VIDEO);
            } else {
                returnTo.returnToWork(false);
            }
        });
    }

    public static boolean isVideoReady() {
        return Appodeal.isLoaded(Appodeal.REWARDED_VIDEO);
    }

    public static boolean isVideoInitialized() {
        return Appodeal.isInitialized(Appodeal.REWARDED_VIDEO);
    }
}
