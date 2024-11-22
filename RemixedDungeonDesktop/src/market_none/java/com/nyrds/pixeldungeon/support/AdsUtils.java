package com.nyrds.pixeldungeon.support;

import android.view.View;
import android.webkit.WebView;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.game.Game;

import java.util.HashMap;
import java.util.Map;

public class AdsUtils {

    static final Map<AdsUtilsCommon.IBannerProvider, Integer> bannerFails = new HashMap<>();
    static final Map<AdsUtilsCommon.IInterstitialProvider, Integer> interstitialFails = new HashMap<>();
    static final Map<AdsUtilsCommon.IRewardVideoProvider, Integer> rewardVideoFails = new HashMap<>();

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
            if (view instanceof WebView) {
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

    public static void removeBannerView(int index, View adview) {
        Game.instance().getLayout().removeViewAt(index);
    }
}
