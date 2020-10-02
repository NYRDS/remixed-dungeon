package com.nyrds.pixeldungeon.support;

import android.view.View;
import android.webkit.WebView;

import com.appodeal.ads.BannerView;
import com.nyrds.android.RemixedDungeonApp;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Game;

import java.util.HashMap;
import java.util.Map;

public class AdsUtils {

    static Map<AdsUtilsCommon.IBannerProvider, Integer> bannerFails = new HashMap<>();
    static Map<AdsUtilsCommon.IInterstitialProvider, Integer> interstitialFails = new HashMap<>();
    static Map<AdsUtilsCommon.IRewardVideoProvider, Integer> rewardVideoFails = new HashMap<>();

    static {
        if(!RemixedDungeonApp.checkOwnSignature()) {
            bannerFails.put(new AAdsComboProvider(), 0);
            interstitialFails.put(new AAdsComboProvider(), 0);
        }
    }


    public static void initRewardVideo() {
        if(!rewardVideoFails.isEmpty()) {
            return;
        }
    }

    static int bannerIndex() {
        int childs = Game.instance().getLayout().getChildCount();
        for (int i = 0; i < childs; ++i) {
            View view = Game.instance().getLayout().getChildAt(i);
            if (view instanceof WebView || view instanceof BannerView) {
                return i;
            }
        }
        return -1;
    }

    static void updateBanner(final View view) {
        Game.instance().runOnUiThread(() -> {

            int index = bannerIndex();
            if (index >= 0) {

                View adview = Game.instance().getLayout().getChildAt(index);
                if(adview == view) {
                    return;
                }

                removeBannerView(index, adview);
            }

            try {
                Game.instance().getLayout().addView(view, 0);
            } catch (IllegalStateException e) {
                EventCollector.logException(e);
            }
        });
    }

    public static void removeTopBanner() {
        Game.instance().runOnUiThread(() -> {
            int index = bannerIndex();
            if (index >= 0) {

                View adview = Game.instance().getLayout().getChildAt(index);

                removeBannerView(index, adview);
            }
        });
    }

    private static void removeBannerView(int index, View adview) {
        if (adview instanceof BannerView) {
        }

        Game.instance().getLayout().removeViewAt(index);
    }
}
