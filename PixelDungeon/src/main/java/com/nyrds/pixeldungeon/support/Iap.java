package com.nyrds.pixeldungeon.support;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.nyrds.android.google.util.IabHelper;
import com.nyrds.android.google.util.IabResult;
import com.nyrds.android.google.util.Inventory;
import com.nyrds.android.google.util.Purchase;
import com.nyrds.android.google.util.SkuDetails;
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.PixelDungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by mike on 24.05.2016.
 */
public class Iap {
    static final int RC_REQUEST = (int) (Math.random() * 0xffff);

    private static final String SKU_LEVEL_1 = "supporter_level_1";
    private static final String SKU_LEVEL_2 = "supporter_level_2";
    private static final String SKU_LEVEL_3 = "supporter_level_3";

    private static IabHelper mHelper = null;
    private static Inventory mInventory = null;

    private static Activity mContext;

    private static volatile boolean m_iapReady = false;

    private static IapCallback mIapCallback = null;

    public static boolean googleIapUsable() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS;
    }

    public static boolean isReady() {
        return m_iapReady;
    }

    private static void initIapPhase2() {
        if (mHelper != null) {
            return;
        }

        String base64EncodedPublicKey = Game.getVar(R.string.iapKey);

        mHelper = new IabHelper(mContext, base64EncodedPublicKey);

        //mHelper.enableDebugLogging(BuildConfig.DEBUG);
        mHelper.enableDebugLogging(true);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()) {
                    complain("Problem setting up in-app billing: " + result);

	                if(result.getResponse() == mHelper.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE ) {
		                EventCollector.logEvent("iap","No billing on device");
		                return;
	                }

	                try {
		                mHelper.disposeWhenFinished();
	                } catch (IllegalArgumentException e) {
		                EventCollector.logException(e,"damn iab lib");

	                }


                    mHelper = null;
                    m_iapReady = false;
                    initIap(mContext);
                    return;
                }

                queryItems();
            }
        });
    }

    public static void queryItemsPrice(List<String> items) {
        try {
            mHelper.queryInventoryAsync(true, items, null, mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            EventCollector.logException(e);
        } catch (IllegalStateException e) {
	        EventCollector.logException(e, "damn iab lib");
        }
    }

    private static void queryItems() {
        List<String> items = new ArrayList<>();

        items.add(SKU_LEVEL_1);
        items.add(SKU_LEVEL_2);
        items.add(SKU_LEVEL_3);

        List<String> accessories = Accessory.getAccessoriesList();

        for (String item : accessories) {
            items.add(item.toLowerCase(Locale.ROOT));
        }

        queryItemsPrice(items);
    }


    public static void initIap(Activity context) {
        mContext = context;

        if (!googleIapUsable()) {
            return; // no services - no iap :(
        }

        new Thread() {
            @Override
            public void run() {
                if (Util.isConnectedToInternet()) {
                    initIapPhase2();
                }
            }
        }.start();
    }


    public static boolean checkPurchase(String item) {
        Purchase check = mInventory.getPurchase(item.toLowerCase(Locale.ROOT));
	    return check != null && verifyDeveloperPayload(check);

    }

    private static void checkPurchases() {
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
    }

    static IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null)
                return;

            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            mInventory = inventory;
            checkPurchases();
            Accessory.check();
            m_iapReady = true;
        }
    };

    @Nullable
    static String formatSkuPrice(SkuDetails sku) {
        if (sku == null) {
            return null;
        }
        return sku.getPrice();
    }

    @Nullable
    public static String getSkuPrice(String item) {
        if (mInventory == null) {
            return null;
        }

        return formatSkuPrice(mInventory.getSkuDetails(item.toLowerCase(Locale.ROOT)));
    }

    @Nullable
    public static String getDonationPriceString(int level) {
        if (mInventory == null) {
            return null;
        }

        switch (level) {
            case 1:
                return formatSkuPrice(mInventory.getSkuDetails(SKU_LEVEL_1));
            case 2:
                return formatSkuPrice(mInventory.getSkuDetails(SKU_LEVEL_2));
            case 3:
                return formatSkuPrice(mInventory.getSkuDetails(SKU_LEVEL_3));
        }
        return null;
    }


    public synchronized static void doPurchase(String sku, IapCallback callback) {
        /*if(BuildConfig.DEBUG) {
            callback.onPurchaseOk();
			return;
		}*/

        if (!isReady()) {
            EventCollector.logEvent("fail", "purchase not ready");
            return;
        }
        m_iapReady = false;

        String payload = "";
        mIapCallback = callback;


        try {
            mHelper.launchPurchaseFlow(mContext, sku.toLowerCase(Locale.ROOT), RC_REQUEST,
                    new IabHelper.OnIabPurchaseFinishedListener() {
                        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                            m_iapReady = true;
                            if (mHelper == null)
                                return;

                            if (result.isFailure()) {
                                complain("Error purchasing: " + result);
                                return;
                            }

                            if (!verifyDeveloperPayload(purchase)) {
                                complain("Error purchasing. Authenticity verification failed.");
                                return;
                            }

                            if (mIapCallback == null) {
                                if (purchase.getSku().equals(SKU_LEVEL_1)) {
                                    PixelDungeon.setDonationLevel(1);
                                }

                                if (purchase.getSku().equals(SKU_LEVEL_2)) {
                                    PixelDungeon.setDonationLevel(2);
                                }

                                if (purchase.getSku().equals(SKU_LEVEL_3)) {
                                    PixelDungeon.setDonationLevel(3);
                                }
                            } else {
                                Game.executeInGlThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mIapCallback.onPurchaseOk();
                                        mIapCallback = null;
                                    }
                                });
                            }
                        }
                    },
                    payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            EventCollector.logException(e);
        }
    }

    static boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        return true;
    }

    public static void donate(int level) {
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
        }
    }

    static void complain(String message) {
        EventCollector.logEvent("iap error", message);
        Log.e("GAME", "**** IAP Error: " + message);
    }

    public static boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null) return false;

        // Pass on the activity result to the helper for handling
        return mHelper.handleActivityResult(requestCode, resultCode, data);
    }

    public interface IapCallback {
        void onPurchaseOk();
    }

}
