package com.nyrds.pixeldungeon.support;

import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerView;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.util.StringsManager;
import com.yandex.mobile.ads.banner.BannerAdView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



//This is flavor specific class
public class AdsUtils {

    static Map<AdsUtilsCommon.IBannerProvider, Integer> bannerFails = new ConcurrentHashMap<>();
    static Map<AdsUtilsCommon.IInterstitialProvider, Integer> interstitialFails = new ConcurrentHashMap<>();
    static Map<AdsUtilsCommon.IRewardVideoProvider, Integer> rewardVideoFails = new ConcurrentHashMap<>();

    static InitializationStatus initializationStatus;
    static boolean YandexInitialized = false;

    static {
        try {
            com.yandex.mobile.ads.common.MobileAds.initialize(RemixedDungeonApp.getContext(), () -> {
                YandexInitialized = true;
                EventCollector.logEvent("yandex_initialized");

            });

            if (!GamePreferences.uiLanguage().equals("ru")) {
                //AdMob
                com.google.android.gms.ads.MobileAds.initialize(RemixedDungeonApp.getContext(), initializationStatus -> {
                    AdsUtils.initializationStatus = initializationStatus;
                    var status = initializationStatus.getAdapterStatusMap();

                    String statusString = "";
                    for (var entry : status.entrySet()) {
                        statusString += entry.getKey() + " : " + entry.getValue().getInitializationState() + "\n";
                    }

                    EventCollector.logEvent("AdMob", "status", statusString);
                });

                bannerFails.put(new AdMobBannerProvider(StringsManager.getVar(R.string.easyModeAdUnitId)), -3);
                interstitialFails.put(new AdMobInterstitialProvider(StringsManager.getVar(R.string.saveLoadAdUnitId)), -3);
            }

            bannerFails.put(new YandexBannerProvider(StringsManager.getVar(R.string.banner_yandex)), -2);
            interstitialFails.put(new YandexInterstitialProvider(StringsManager.getVar(R.string.interstitial_yandex)), -2);

            if (!RemixedDungeonApp.checkOwnSignature()) {
                bannerFails.put(new AAdsComboProvider(), 0);
                interstitialFails.put(new AAdsComboProvider(), 0);
            }

            if (AppodealAdapter.usable()) {
                AppodealAdapter.init();
                bannerFails.put(AppodealBannerProvider.getInstance(), -1);
                interstitialFails.put(new AppodealInterstitialProvider(), -1);
            }
        } catch (Exception e) {
            EventCollector.logException(e,"AdsUtils init error");
        }
    }


    public static void initRewardVideo() {
        try {
            if (!rewardVideoFails.isEmpty()) {
                return;
            }
            if (AppodealAdapter.usable()) {
                rewardVideoFails.put(new AppodealRewardVideoProvider(), -1);
            }

            rewardVideoFails.put(new YandexRewardVideoAds(StringsManager.getVar(R.string.rewarded_yandex)), -2);

            if (!GamePreferences.uiLanguage().equals("ru")) {
                rewardVideoFails.put(new GoogleRewardVideoAds(StringsManager.getVar(R.string.cinemaRewardAdUnitId)), -20);
            }

        } catch (Exception e) {
            EventCollector.logException(e,"AdsUtils init rw");
        }
    }

    static void removeBannerView(int index, View adview) {
        boolean bannerFound = false;
        if (adview instanceof BannerView) {
            Appodeal.hide(Game.instance(), Appodeal.BANNER);
            bannerFound = true;
        }
        if (adview instanceof AdView) {
            ((AdView) adview).destroy();
            bannerFound = true;
        }
        if(adview instanceof BannerAdView) {
            ((BannerAdView) adview).destroy();
            bannerFound = true;
        }
        if(adview instanceof WebView) {
            ((WebView) adview).destroy();
            bannerFound = true;
        }

        if(bannerFound) {
            Game.instance().getLayout().removeViewAt(index);
        }
    }

    static int bannerIndex() {
        final LinearLayout layout = Game.instance().getLayout();

        int childs = layout.getChildCount();
        for (int i = 0; i < childs; ++i) {
            View view = layout.getChildAt(i);
            if (view instanceof AdView || view instanceof WebView || view instanceof BannerView || view instanceof BannerAdView) {
                return i;
            }
        }
        return -1;
    }

}
