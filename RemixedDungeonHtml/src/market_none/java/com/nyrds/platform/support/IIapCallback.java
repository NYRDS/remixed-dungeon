package com.nyrds.platform.support;

/**
 * Callback interface for IAP operations
 */
public interface IIapCallback {
    void onPurchaseOk();
    
    default void onPurchaseFail() {
        // Default empty implementation
    }
}