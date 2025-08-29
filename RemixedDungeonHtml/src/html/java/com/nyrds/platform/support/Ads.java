package com.nyrds.platform.support;

/**
 * HTML version of Ads
 */
public class Ads {
    public static void showInterstitial() {
        // In HTML version, ads are not supported
        System.out.println("Ads not supported in HTML version");
    }
    
    public static void showRewardedVideo() {
        // In HTML version, ads are not supported
        System.out.println("Rewarded video not supported in HTML version");
    }
    
    public static boolean isReady() {
        // In HTML version, ads are not supported
        return false;
    }
    
    // Additional methods needed for HTML version
    public static void removeEasyModeBanner() {
        // In HTML version, ads are not supported
        System.out.println("Easy mode banner removal not supported in HTML version");
    }
    
    public static boolean isRewardVideoReady() {
        // In HTML version, ads are not supported
        return false;
    }
    
    public static void displayEasyModeBanner() {
        // In HTML version, ads are not supported
        System.out.println("Easy mode banner display not supported in HTML version");
    }
}