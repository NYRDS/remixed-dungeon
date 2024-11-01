package com.nyrds.pixeldungeon.support;

import android.view.View;
import android.widget.LinearLayout;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.watabou.noosa.InterstitialPoint;

/**
 * Created by mike on 24.05.2016.
 */
public class Ads {

    private static boolean isSmallScreen() {
        return (Game.width() < 400 || Game.height() < 400);
    }

    public static void displayEasyModeBanner() {
        if (!isSmallScreen()) {
            AdsUtilsCommon.displayTopBanner();
        }
    }

    public static boolean isRewardVideoReady(){
        return AdsUtilsCommon.isRewardVideoReady();
    }

    public static void showRewardVideo(final InterstitialPoint work) {
        AdsUtilsCommon.showRewardVideo(work);
    }

    public static void displaySaveAndLoadAd(final InterstitialPoint work) {
        AdsUtilsCommon.showInterstitial(work);
    }

    public static void removeEasyModeBanner() {
        GameLoop.runOnMainThread(() -> {
            int index = AdsUtils.bannerIndex();
            if (index >= 0) {
                View adview = Game.instance().getLayout().getChildAt(index);
                AdsUtils.removeBannerView(index, adview);
            }
        });
    }

    static void updateBanner(final View view) {
        GameLoop.runOnMainThread(() -> {

            int index = AdsUtils.bannerIndex();
            final LinearLayout layout = Game.instance().getLayout();

            if (index >= 0) {

                View adview = layout.getChildAt(index);
                if(adview == view) {
                    return;
                }
                AdsUtils.removeBannerView(index, adview);
            }

            try {
                layout.addView(view, 0);
            } catch (IllegalStateException e) {
                EventCollector.logException(e);
            }
        });
    }
}
