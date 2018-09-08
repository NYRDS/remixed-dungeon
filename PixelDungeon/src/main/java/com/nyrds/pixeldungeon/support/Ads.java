package com.nyrds.pixeldungeon.support;

import com.nyrds.android.util.Util;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.PixelDungeon;

/**
 * Created by mike on 24.05.2016.
 */
public class Ads {

    private static boolean isSmallScreen() {
        return (Game.width() < 400 || Game.height() < 400);
    }

    private static boolean needDisplaySmallScreenEasyModeIs() {
        return Game.getDifficulty() == 0 && isSmallScreen() && PixelDungeon.donated() == 0;
    }

    public static void displayEasyModeBanner() {
        if (AdMob.googleAdsUsable() && Util.isConnectedToInternet()) {
            if (isSmallScreen()) {
                AdMob.initEasyModeIntersitial();
            } else {
                AdsUtilsCommon.displayTopBanner();
            }
        }
    }

    public static void initSaveAndLoadIntersitial() {
        AdMob.initSaveAndLoadIntersitial();
    }

    public static void displaySaveAndLoadAd(final InterstitialPoint work) {
        AdMob.displayIsAd(work, AdMob.mSaveAndLoadAd);
    }

    public static void displayEasyModeSmallScreenAd(final InterstitialPoint work) {
        if (needDisplaySmallScreenEasyModeIs()) {
            AdMob.displayIsAd(work, AdMob.mEasyModeSmallScreenAd);
        } else {
            work.returnToWork(true);
        }
    }


}
