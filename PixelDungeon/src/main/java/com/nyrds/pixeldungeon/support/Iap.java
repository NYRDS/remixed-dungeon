package com.nyrds.pixeldungeon.support;

import android.app.Activity;

import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.nyrds.pixeldungeon.support.Google.GoogleIap;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.PixelDungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;

/**
 * Created by mike on 24.05.2016.
 */
public class Iap implements IPurchasesUpdated {

    private static final String SKU_LEVEL_1 = "supporter_level_1";
    private static final String SKU_LEVEL_2 = "supporter_level_2";
    private static final String SKU_LEVEL_3 = "supporter_level_3";
    private static final String SKU_LEVEL_4 = "supporter_level_4";

    private GoogleIap mIap;

    private IIapCallback mIapCallback = null;

    public Iap(Activity context) {
        mIap = new GoogleIap(context, this);

        List<String> items = new ArrayList<>();

        items.add(SKU_LEVEL_1);
        items.add(SKU_LEVEL_2);
        items.add(SKU_LEVEL_3);
        items.add(SKU_LEVEL_4);

        List<String> accessories = Accessory.getAccessoriesList();

        for (String item : accessories) {
            items.add(item.toLowerCase(Locale.ROOT));
        }

        mIap.querySkuList(items);

        mIap.queryPurchases();
    }

    public boolean checkPurchase(String item) {
	    return mIap.checkPurchase(item);
    }

    private void checkPurchases() {
        PixelDungeon.setDonationLevel(0);

        if (checkPurchase(SKU_LEVEL_1)) {
            PixelDungeon.setDonationLevel(1);
        }

        if (checkPurchase(SKU_LEVEL_2)) {
            PixelDungeon.setDonationLevel(2);
        }

        if (checkPurchase(SKU_LEVEL_3)) {
            PixelDungeon.setDonationLevel(3);
        }

        if (checkPurchase(SKU_LEVEL_4)) {
            PixelDungeon.setDonationLevel(4);
        }
    }

    @Nullable
    public String getDonationPriceString(int level) {

        switch (level) {
            case 1:
                return mIap.getSkuPrice(SKU_LEVEL_1);
            case 2:
                return mIap.getSkuPrice(SKU_LEVEL_2);
            case 3:
                return mIap.getSkuPrice(SKU_LEVEL_3);
            case 4:
                return mIap.getSkuPrice(SKU_LEVEL_4);
        }
        return null;
    }


    public void doPurchase(String sku, IIapCallback callback) {
        mIapCallback = callback;
	    mIap.doPurchase(sku.toLowerCase(Locale.ROOT));
    }


    public void donate(int level) {
        switch (level) {
            case 1:
                doPurchase(SKU_LEVEL_1, null);
                break;
            case 2:
                doPurchase(SKU_LEVEL_2, null);
                break;
            case 3:
                doPurchase(SKU_LEVEL_3, null);
                break;
            case 4:
                doPurchase(SKU_LEVEL_4, null);
                break;
        }
    }

    @Override
    public void onPurchasesUpdated() {
        checkPurchases();
        Accessory.check();

        if(mIapCallback!=null) {
            Game.pushUiTask(new Runnable() {
                @Override
                public void run() {
                    mIapCallback.onPurchaseOk();
                    mIapCallback = null;
                }
            });
        }
    }

    public String getSkuPrice(String item) {
        return mIap.getSkuPrice(item);
    }

    public boolean isReady() {
        return mIap.isServiceConnected();
    }
}
