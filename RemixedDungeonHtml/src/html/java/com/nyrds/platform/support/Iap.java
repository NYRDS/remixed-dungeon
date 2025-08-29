package com.nyrds.platform.support;

/**
 * HTML version of Iap
 */
public class Iap {
    public static boolean isPremium() {
        // In HTML version, we assume user is not premium
        return false;
    }
    
    public static void purchasePremium() {
        // In HTML version, purchases are not supported
        System.out.println("IAP not supported in HTML version");
    }
}