package com.nyrds.pixeldungeon.support;

import java.util.HashMap;
import java.util.Map;
import com.nyrds.pixeldungeon.support.AdsUtilsCommon;
public class AdsUtils {
    static final Map<AdsUtilsCommon.IBannerProvider, Integer> bannerFails = new HashMap<>();
    static final Map<AdsUtilsCommon.IInterstitialProvider, Integer> interstitialFails = new HashMap<>();
    static final Map<AdsUtilsCommon.IRewardVideoProvider, Integer> rewardVideoFails = new HashMap<>();


    public static void initRewardVideo() {
    }

    static int bannerIndex() {
        return -1;
    }

    static void updateBanner(final Object view) {

    }

    public static void removeTopBanner() {
    }

    public static void removeBannerView(int index, Object adview) {

    }

}
