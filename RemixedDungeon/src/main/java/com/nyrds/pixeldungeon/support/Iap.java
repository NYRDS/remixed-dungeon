package com.nyrds.pixeldungeon.support;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by mike on 24.05.2016.
 */
public class Iap implements IPurchasesUpdated {

    private static final String SKU_LEVEL_1 = "supporter_level_1";
    private static final String SKU_LEVEL_2 = "supporter_level_2";
    private static final String SKU_LEVEL_3 = "supporter_level_3";
    private static final String SKU_LEVEL_4 = "supporter_level_4";

    private IapAdapter mIap;

    private IIapCallback mIapCallback = null;

    public Iap() {
        mIap = new IapAdapter(Game.instance(), this);

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
        GamePreferences.setDonationLevel(0);

        if (checkPurchase(SKU_LEVEL_1)) {
            GamePreferences.setDonationLevel(1);
        }

        if (checkPurchase(SKU_LEVEL_2)) {
            GamePreferences.setDonationLevel(2);
        }

        if (checkPurchase(SKU_LEVEL_3)) {
            GamePreferences.setDonationLevel(3);
        }

        if (checkPurchase(SKU_LEVEL_4)) {
            GamePreferences.setDonationLevel(4);
        }
    }

    @NotNull
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
        return "";
    }


    public void doPurchase(@NotNull String sku, IIapCallback callback) {
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
            GameLoop.pushUiTask(() -> {
                if(mIapCallback!=null) {
                    mIapCallback.onPurchaseOk();
                    mIapCallback = null;
                } else {
                    EventCollector.logException("mIapCallback disappeared");
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
