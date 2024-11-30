package com.nyrds.pixeldungeon.support;

import android.app.Activity;
import android.content.Intent;

import com.nyrds.pixeldungeon.support.di.PaymentsModule;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.List;

import ru.rustore.sdk.billingclient.RuStoreBillingClient;

class IapAdapter {
    // Previously created with RuStoreBillingClientFactory.create()
    private RuStoreBillingClient billingClient = PaymentsModule.provideRuStorebillingClient();
    public IapAdapter(Activity context, Iap iap) {

    }

    public void querySkuList(List<String> items) {

    }

    public void queryPurchases() {

    }

    public boolean checkPurchase(String item) {
        return false;
    }

    public String getSkuPrice(String skuLevel1) {
        return Utils.EMPTY_STRING;
    }

    public void doPurchase(String toLowerCase) {

    }

    public boolean isServiceConnected() {
        return false;
    }

    public void onNewIntent(Intent intent) {
        billingClient.onNewIntent(intent);
    }
}
