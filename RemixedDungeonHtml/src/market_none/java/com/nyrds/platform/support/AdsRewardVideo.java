package com.nyrds.platform.support;

/**
 * HTML version of AdsRewardVideo with market_none flavor
 */
public class AdsRewardVideo {
    public static void init() {
        // Reward video ads are not supported in HTML version
        System.out.println("Reward video initialization not supported in HTML version");
    }

    public static boolean isReady() {
        // Reward videos are not ready in HTML version
        return false;
    }

    public static void show() {
        // Reward videos are not supported in HTML version
        System.out.println("Reward video not supported in HTML version");
    }

    public static void show(Object reward) {
        // Reward videos are not supported in HTML version
        System.out.println("Reward video with reward not supported in HTML version");
    }
}