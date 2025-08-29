package com.nyrds.platform.support;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.nyrds.platform.game.Game;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * HTML version of Iap with market_none flavor
 */
public class Iap {
    private static final String SKU_LEVEL_1 = "supporter_level_1";
    private static final String SKU_LEVEL_2 = "supporter_level_2";
    private static final String SKU_LEVEL_3 = "supporter_level_3";
    private static final String SKU_LEVEL_4 = "supporter_level_4";

    public Iap() {
        // Initialize with no purchases in HTML version
    }

    public boolean checkPurchase(String item) {
        // In HTML version, we assume no purchases
        return false;
    }

    private void checkPurchases() {
        // No purchases to check in HTML version
    }

    @NotNull
    public String getDonationPriceString(int level) {
        // Return empty string as purchases are not supported in HTML
        return "";
    }

    public void doPurchase(@NotNull String sku, IIapCallback callback) {
        // In HTML version, purchases are not supported
        System.out.println("IAP purchase not supported in HTML version");
        if (callback != null) {
            GameLoop.pushUiTask(() -> callback.onPurchaseFail());
        }
    }

    public void donate(int level) {
        // In HTML version, donations are not supported
        System.out.println("IAP donation not supported in HTML version");
    }

    public String getSkuPrice(String item) {
        // Return empty string as purchases are not supported in HTML
        return "";
    }

    public boolean isReady() {
        // IAP is not ready in HTML version
        return false;
    }

    public void onNewIntent(Object intent) {
        // No intent handling in HTML version
    }
}