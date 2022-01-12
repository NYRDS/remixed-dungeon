package com.nyrds.pixeldungeon.support;

import android.view.View;

import com.nyrds.platform.game.Game;

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
        Game.instance().getLayout().removeViewAt(index);
    }
}
