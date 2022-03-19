package com.nyrds.pixeldungeon.support;

import android.view.View;

import com.nyrds.platform.util.PUtil;

import java.util.HashMap;
import java.util.Map;

public class AdsUtils {

    static Map<AdsUtilsCommon.IBannerProvider, Integer> bannerFails = new HashMap<>();
    static Map<AdsUtilsCommon.IInterstitialProvider, Integer> interstitialFails = new HashMap<>();
    static Map<AdsUtilsCommon.IRewardVideoProvider, Integer> rewardVideoFails = new HashMap<>();


    public static void initRewardVideo() {
    }

    static int bannerIndex() {
        return -1;
    }


    public static void removeBannerView(int index, View adview) {
        PUtil.slog("", "removeBannerView");
    }

    public static void removeEasyModeBanner() {
        PUtil.slog("", "removeEasyModeBanner");
    }

    static void updateBanner(final View view) {
        PUtil.slog("", "updateBanner");
    }

}
