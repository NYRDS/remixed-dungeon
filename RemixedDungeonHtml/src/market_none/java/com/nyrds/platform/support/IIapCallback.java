package com.nyrds.platform.support;

/**
 * Callback interface for IAP operations
 */
public interface IIapCallback {
    void onPurchaseOk();
    void onPurchaseFail();
}