package com.nyrds.pixeldungeon.support;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.RewardedVideoCallbacks;
import com.appodeal.ads.utils.Log;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.PixelDungeon;


/**
 * Created by mike on 18.02.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class AppodealAdapter {
    private static InterstitialPoint returnTo;

    public static void init() {

        final int toInitialize = Appodeal.INTERSTITIAL | Appodeal.BANNER | Appodeal.REWARDED_VIDEO;
        final int toCache = Appodeal.INTERSTITIAL | Appodeal.BANNER;
        final int notToCache = Appodeal.REWARDED_VIDEO;

        if (Appodeal.isInitialized(Appodeal.BANNER)) {
            return;
        }

        String appKey = Game.getVar(R.string.appodealRewardAdUnitId);

        //vungle disable due to strange build issue
        //mopub, mobvista & tapjoy due audiences mismatch
        //ogury - intersiteal
        String disableNetworks[] = {"adcolony","facebook", "flurry", "startapp", "vungle", "mopub", "mobvista", "tapjoy", "ogury"};

        for (String net : disableNetworks) {
            Appodeal.disableNetwork(PixelDungeon.instance(), net);
        }
        Appodeal.disableLocationPermissionCheck();


        if (BuildConfig.DEBUG) {
            Appodeal.setLogLevel(Log.LogLevel.verbose);
            //Appodeal.setTesting(true);
        }

        Appodeal.initialize(PixelDungeon.instance(), appKey, toInitialize, EuConsent.getConsentLevel() == EuConsent.PERSONALIZED);
        Appodeal.setAutoCache(notToCache, false);
        Appodeal.cache(PixelDungeon.instance(), toCache);
        Appodeal.setAutoCache(toCache, true);
    }


    public static void initRewardedVideo() {

        Game.instance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                init();

                EventCollector.startTrace("appodeal reward video");

                Appodeal.cache(PixelDungeon.instance(), Appodeal.REWARDED_VIDEO);
                Appodeal.setAutoCache(Appodeal.REWARDED_VIDEO, true);

                Appodeal.setRewardedVideoCallbacks(new RewardedVideoCallbacks() {

                    @Override
                    public void onRewardedVideoLoaded(boolean b) {
                        EventCollector.stopTrace("appodeal reward video", "appodeal reward video", "ok", "");
                    }

                    @Override
                    public void onRewardedVideoFailedToLoad() {
                        EventCollector.stopTrace("appodeal reward video", "appodeal reward video", "fail", "");
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
            }
        });
    }

    public static void showCinemaRewardVideo(InterstitialPoint ret) {
        returnTo = ret;
        Game.instance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isVideoReady()) {
                    Appodeal.show(PixelDungeon.instance(), Appodeal.REWARDED_VIDEO);
                } else {
                    returnTo.returnToWork(false);
                }
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
