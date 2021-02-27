package com.nyrds.pixeldungeon.support;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.nyrds.android.RemixedDungeonApp;

import java.util.HashMap;
import java.util.Map;

//This is flavor specific class
public class AdsUtils {

    static Map<AdsUtilsCommon.IBannerProvider, Integer> bannerFails = new HashMap<>();
    static Map<AdsUtilsCommon.IInterstitialProvider, Integer> interstitialFails = new HashMap<>();
    static Map<AdsUtilsCommon.IRewardVideoProvider, Integer> rewardVideoFails = new HashMap<>();

    static InitializationStatus initializationStatus;

    static {
        MobileAds.initialize(RemixedDungeonApp.getContext(), initializationStatus -> {
            AdsUtils.initializationStatus = initializationStatus;
            initializationStatus.getAdapterStatusMap();

            bannerFails.put(new AdMobComboProvider(),-2);
            interstitialFails.put(new AdMobComboProvider(), -2);
        });



        if(!RemixedDungeonApp.checkOwnSignature()) {
            bannerFails.put(new AAdsComboProvider(), 0);
            interstitialFails.put(new AAdsComboProvider(), 0);
        }

        if(AppodealAdapter.usable()) {
            AppodealAdapter.init();
            bannerFails.put(AppodealBannerProvider.getInstance(), -1);
            interstitialFails.put(new AppodealInterstitialProvider(), -1);
        }
    }


    public static void initRewardVideo() {

        if(!rewardVideoFails.isEmpty()) {
            return;
        }

        if(AppodealAdapter.usable()) {
            AppodealRewardVideoProvider.init();
            rewardVideoFails.put(new AppodealRewardVideoProvider(), -1);
        }
        rewardVideoFails.put(new GoogleRewardVideoAds(), -2);
    }

}
