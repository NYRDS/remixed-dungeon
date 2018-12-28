package com.nyrds.pixeldungeon.support;

import android.view.View;
import android.webkit.WebView;

import com.google.android.gms.ads.AdView;
import com.watabou.noosa.Game;

import java.util.HashMap;
import java.util.Map;
public class AdsUtils {

    public static Map<AdsUtilsCommon.IBannerProvider, Integer> bannerFails = new HashMap<>();
    public static Map<AdsUtilsCommon.IInterstitialProvider, Integer> interstitialFails = new HashMap<>();

    static {
        bannerFails.put(new AAdsComboProvider(),-3);
        bannerFails.put(new AdMobComboProvider(),-2);

        interstitialFails.put(new AAdsComboProvider(), -3);
        interstitialFails.put(new AdMobComboProvider(), -2);
    }

    public static int bannerIndex() {
        int childs = Game.instance().getLayout().getChildCount();
        for (int i = 0; i < childs; ++i) {
            View view = Game.instance().getLayout().getChildAt(i);
            if (view instanceof AdView || view instanceof WebView ) {
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

                    Game.instance().getLayout().removeViewAt(index);
                }
                Game.instance().getLayout().addView(view,0);
            }

        });
    }

    public static void removeTopBanner() {
        Game.instance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = bannerIndex();
                if (index >= 0) {
                    Game.instance().getLayout().removeViewAt(index);
                }
            }

        });
    }
}
