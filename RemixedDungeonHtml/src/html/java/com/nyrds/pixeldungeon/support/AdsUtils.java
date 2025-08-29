package com.nyrds.pixeldungeon.support;

import java.util.HashMap;
import java.util.Map;

/**
 * HTML version of AdsUtils
 */
public class AdsUtils {
    static final Map<Object, Integer> bannerFails = new HashMap<>();
    static final Map<Object, Integer> interstitialFails = new HashMap<>();
    static final Map<Object, Integer> rewardVideoFails = new HashMap<>();
    
    public static void init() {
        // In HTML version, ads initialization is not needed
        System.out.println("Ads initialization not needed in HTML version");
    }
    
    public static void showBanner() {
        // In HTML version, banner ads are not supported
        System.out.println("Banner ads not supported in HTML version");
    }
    
    public static void hideBanner() {
        // In HTML version, banner ads are not supported
        System.out.println("Banner ads not supported in HTML version");
    }
    
    public static void initRewardVideo() {
        // In HTML version, reward video initialization is not needed
        System.out.println("Reward video initialization not needed in HTML version");
    }
    
    static int bannerIndex() {
        // In HTML version, banner index is not applicable
        return -1;
    }
}