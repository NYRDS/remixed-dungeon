package com.nyrds.pixeldungeon.support;

import android.view.View;
import android.webkit.WebView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerView;
import com.google.android.gms.ads.AdView;
import com.watabou.noosa.Game;

import java.util.HashMap;
import java.util.Map;

public class AdsUtils {

    public static Map<AdsUtilsCommon.IBannerProvider, Integer> fails = new HashMap<>();

    static {
        fails.put(new AAdsBannerProvider(),0);
        fails.put(new AppodealBannerProvider(),0);
        fails.put(new AdMobBannerProvider(),0);
    }

    private static int bannerIndex() {
        int childs = Game.instance().getLayout().getChildCount();
        for (int i = 0; i < childs; ++i) {
            View view = Game.instance().getLayout().getChildAt(i);
            if (view instanceof AdView || view instanceof WebView || view instanceof BannerView) {
                return i;
            }
        }
        return -1;
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

                    Game.instance().getLayout().removeViewAt(index);
                }
            }

        });
    }
}
