package com.nyrds.platform.support;

public class Iap {
    public boolean checkPurchase(String item) {
        return false;
    }

    public boolean isReady() {
        return false;
    }

    public String getSkuPrice(String item) {
        return "";
    }

    public String getDonationPriceString(int level) {
        return "";
    }

    public void donate(int level) {
    }

    public void doPurchase(String accessory, IIapCallback callback) {
    }
}
