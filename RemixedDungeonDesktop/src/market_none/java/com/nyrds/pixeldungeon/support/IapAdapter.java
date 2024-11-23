package com.nyrds.pixeldungeon.support;


import com.watabou.pixeldungeon.utils.Utils;

import java.util.List;

class IapAdapter {
    public IapAdapter(Object context, Object iap) {

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

    public void onNewIntent(Object intent) {
    }
}
