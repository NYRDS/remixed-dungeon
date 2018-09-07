package com.nyrds.pixeldungeon.support;

import android.view.View;
import android.webkit.WebView;

import com.google.android.gms.ads.AdView;
import com.watabou.noosa.Game;

import java.util.HashMap;
import java.util.Map;

public class AdsUtils {


    private static Map<IBannerProvider, Integer> fails = new HashMap<>();

    static {
        fails.put(new AdMobBannerProvider(),0);
    }

    private static int bannerIndex() {
        int childs = Game.instance().getLayout().getChildCount();
        for (int i = 0; i < childs; ++i) {
            View view = Game.instance().getLayout().getChildAt(i);
            if (view instanceof AdView || view instanceof WebView) {
                return i;
            }
        }
        return -1;
    }


    static public void bannerFailed(IBannerProvider provider) {
        if(fails.containsKey(provider)) {
            fails.put(provider,fails.get(provider)+1);
        }   else {
            fails.put(provider,1);
        }
        tryNextBanner();
    }

    private static void tryNextBanner() {

        removeTopBanner();
        int minima = 3;

        IBannerProvider chosenProvider = null;

        for (IBannerProvider provider:fails.keySet()) {
            if(fails.get(provider)<minima) {
                minima = fails.get(provider);
                chosenProvider = provider;
            }
        }

        if(minima < 3 && chosenProvider!=null) {
            final IBannerProvider finalChosenProvider = chosenProvider;
            Game.instance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finalChosenProvider.displayBanner();
                }
            });
        }
    }

    static void displayTopBanner() {
        tryNextBanner();
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


    interface IBannerProvider {
        void displayBanner();
    }
}
