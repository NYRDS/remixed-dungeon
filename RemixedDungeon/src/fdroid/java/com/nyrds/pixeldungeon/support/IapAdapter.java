package com.nyrds.pixeldungeon.support;

import com.watabou.pixeldungeon.utils.Utils;

import java.util.List;

public class IapAdapter {
    public IapAdapter(Object context, Iap iap) {

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
}
