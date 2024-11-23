package com.nyrds.platform.support;

import com.watabou.noosa.ReturnOnlyOnce;

public class Ads {
    public static void removeEasyModeBanner() {}

    public static boolean isRewardVideoReady() {
        return false;
    }

    public static void displayEasyModeBanner() {
    }

    public static void displaySaveAndLoadAd(ReturnOnlyOnce returnOnlyOnce) {
        returnOnlyOnce.returnToWork(true);
    }
}
