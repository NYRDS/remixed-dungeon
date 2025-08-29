package com.nyrds.platform.support;

/**
 * HTML version of Ads with market_none flavor
 */
public class Ads {
    public static void init() {
        // Ads are not supported in HTML version
        System.out.println("Ads initialization not supported in HTML version");
    }

    public static boolean isRewardVideoReady() {
        // Reward videos are not supported in HTML version
        return false;
    }

    public static void showRewardVideo(Object listener) {
        // Reward videos are not supported in HTML version
        System.out.println("Reward video not supported in HTML version");
    }

    public static void removeEasyModeBanner() {
        // Banner ads are not supported in HTML version
        System.out.println("Banner ads not supported in HTML version");
    }

    public static void displayEasyModeBanner() {
        // Banner ads are not supported in HTML version
        System.out.println("Banner ads not supported in HTML version");
    }

    public static boolean isReady() {
        // Ads are not ready in HTML version
        return false;
    }
}