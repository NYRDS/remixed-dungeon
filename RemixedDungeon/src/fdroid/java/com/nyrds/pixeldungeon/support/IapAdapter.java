package com.nyrds.pixeldungeon.support;

import android.app.Activity;
import android.content.Intent;

import com.nyrds.platform.support.Iap;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.List;

public class IapAdapter {
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
    }
}
