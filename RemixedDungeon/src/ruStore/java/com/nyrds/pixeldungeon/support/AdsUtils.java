package com.nyrds.pixeldungeon.support;

import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.util.StringsManager;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.InitializationListener;
import com.yandex.mobile.ads.common.MobileAds;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



//This is flavor specific class
public class AdsUtils {

    static Map<AdsUtilsCommon.IBannerProvider, Integer> bannerFails = new ConcurrentHashMap<>();
    static Map<AdsUtilsCommon.IInterstitialProvider, Integer> interstitialFails = new ConcurrentHashMap<>();
    static Map<AdsUtilsCommon.IRewardVideoProvider, Integer> rewardVideoFails = new ConcurrentHashMap<>();

    static boolean YandexInitialized = false;

    static {
        try {
            MobileAds.initialize(RemixedDungeonApp.getContext(), new InitializationListener() {
                @Override
                public void onInitializationCompleted() {
                    YandexInitialized = true;
                }
            });

            bannerFails.put(new YandexBannerProvider(StringsManager.getVar(R.string.banner_yandex)), -2);
            interstitialFails.put(new YandexInterstitialProvider(StringsManager.getVar(R.string.interstitial_yandex)), -2);
        } catch (Exception e) {
            EventCollector.logException(e,"AdsUtils init error");
        }
    }


    public static void initRewardVideo() {
        try {
            if (!rewardVideoFails.isEmpty()) {
                return;
            }

            rewardVideoFails.put(new YandexRewardVideoAds(StringsManager.getVar(R.string.rewarded_yandex)), -2);
        } catch (Exception e) {
            EventCollector.logException(e,"AdsUtils init rw");
        }
    }

    public static void removeBannerView(int index, View adview) {
        if(adview instanceof BannerAdView) {
            ((BannerAdView) adview).destroy();
        }

        Game.instance().getLayout().removeViewAt(index);
    }

    public static int bannerIndex() {
        final LinearLayout layout = Game.instance().getLayout();

        int childs = layout.getChildCount();
        for (int i = 0; i < childs; ++i) {
            View view = layout.getChildAt(i);
            if (view instanceof WebView || view instanceof BannerAdView) {
                return i;
            }
        }
        return -1;
    }

}
