package com.nyrds.pixeldungeon.support;

import android.view.View;
import android.webkit.WebView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerView;
import com.google.android.gms.ads.AdView;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Game;

import java.util.HashMap;
import java.util.Map;

public class AdsUtils {

    public static Map<AdsUtilsCommon.IBannerProvider, Integer> bannerFails = new HashMap<>();
    public static Map<AdsUtilsCommon.IInterstitialProvider, Integer> interstitialFails = new HashMap<>();


    static {

        if(!Game.instance().checkOwnSignature()) {
            bannerFails.put(new AAdsComboProvider(), -3);
        }

        bannerFails.put(new AdMobComboProvider(),-2);
        bannerFails.put(new AppodealBannerProvider(),-1);

        if(!Game.instance().checkOwnSignature()) {
            interstitialFails.put(new AAdsComboProvider(), -3);
        }

        interstitialFails.put(new AdMobComboProvider(), -2);
        interstitialFails.put(new AppodealInterstitialProvider(), -1);
    }

    public static int bannerIndex() {
        int childs = Game.instance().getLayout().getChildCount();
        for (int i = 0; i < childs; ++i) {
            View view = Game.instance().getLayout().getChildAt(i);
            if (view instanceof AdView || view instanceof WebView || view instanceof BannerView) {
                return i;
            }
        }
        return -1;
    }

    public static void updateBanner(final View view) {
        Game.instance().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int index = bannerIndex();
                if (index >= 0) {

                    View adview = Game.instance().getLayout().getChildAt(index);
                    if(adview == view) {
                        return;
                    }

                    if (adview instanceof BannerView) {
                        Appodeal.hide(Game.instance(), Appodeal.BANNER);
                    }

                    if(adview instanceof AdView) {
                        ((AdView)adview).destroy();
                    }
                    Game.instance().getLayout().removeViewAt(index);
                }

                try {
                    Game.instance().getLayout().addView(view, 0);
                } catch (IllegalStateException e) {
                    EventCollector.logException(e);
                }
            }

        });
    }

    public static void removeTopBanner() {
        Game.instance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = bannerIndex();
                if (index >= 0) {

                    View adview = Game.instance().getLayout().getChildAt(index);

                    if (adview instanceof BannerView) {
                        Appodeal.hide(Game.instance(), Appodeal.BANNER);
                    }
                    if(adview instanceof AdView) {
                        ((AdView)adview).destroy();
                    }

                    Game.instance().getLayout().removeViewAt(index);
                }
            }

        });
    }
}
