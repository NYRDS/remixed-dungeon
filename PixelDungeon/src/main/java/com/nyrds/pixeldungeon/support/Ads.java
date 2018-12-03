package com.nyrds.pixeldungeon.support;

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
        if (!isSmallScreen()) {
            AdsUtilsCommon.displayTopBanner();
        }
    }

    public static void displaySaveAndLoadAd(final InterstitialPoint work) {
        AdsUtilsCommon.showInterstitial(work);
    }

    public static void displayEasyModeSmallScreenAd(final InterstitialPoint work) {
        if (needDisplaySmallScreenEasyModeIs()) {
            AdsUtilsCommon.showInterstitial(work);
        } else {
            work.returnToWork(true);
        }
    }
}
