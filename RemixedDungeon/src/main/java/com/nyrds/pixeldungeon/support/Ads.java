package com.nyrds.pixeldungeon.support;

import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerView;
import com.google.android.gms.ads.AdView;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Game;
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
        Game.instance().runOnUiThread(() -> {
            int index = bannerIndex();
            if (index >= 0) {

                View adview = Game.instance().getLayout().getChildAt(index);
                removeBannerView(index, adview);
            }
        });
    }

    static void removeBannerView(int index, View adview) {
        if (adview instanceof BannerView) {
            Appodeal.hide(Game.instance(), Appodeal.BANNER);
        }
        if (adview instanceof AdView) {
            ((AdView) adview).destroy();
        }

        Game.instance().getLayout().removeViewAt(index);
    }

    static int bannerIndex() {
        final LinearLayout layout = Game.instance().getLayout();

        int childs = layout.getChildCount();
        for (int i = 0; i < childs; ++i) {
            View view = layout.getChildAt(i);
            if (view instanceof AdView || view instanceof WebView || view instanceof BannerView) {
                return i;
            }
        }
        return -1;
    }

    static void updateBanner(final View view) {
        Game.instance().runOnUiThread(() -> {

            int index = bannerIndex();
            final LinearLayout layout = Game.instance().getLayout();

            if (index >= 0) {

                View adview = layout.getChildAt(index);
                if(adview == view) {
                    return;
                }

                removeBannerView(index, adview);
            }

            try {
                layout.addView(view, 0);
            } catch (IllegalStateException e) {
                EventCollector.logException(e);
            }
        });
    }
}
