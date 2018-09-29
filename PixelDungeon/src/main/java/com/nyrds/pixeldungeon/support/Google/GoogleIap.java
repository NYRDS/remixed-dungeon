package com.nyrds.pixeldungeon.support.Google;

import android.content.Context;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.IPurchasesUpdated;
import com.watabou.noosa.Game;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GoogleIap implements PurchasesUpdatedListener {

    private BillingClient mBillingClient;

    private boolean mIsServiceConnected;

    private IPurchasesUpdated mPurchasesUpdatedListener;

    private final Map<String, Purchase> mPurchases = new HashMap<>();

    private Map<String,SkuDetails> mSkuDetails = new HashMap<>();

    public GoogleIap(Context context, IPurchasesUpdated purchasesUpdatedListener) {
        mBillingClient = BillingClient.newBuilder(context).setListener(this).build();
        mPurchasesUpdatedListener = purchasesUpdatedListener;
    }

    public void querySkuList(final List<String> skuList) {
        Runnable queryRequest = new Runnable() {
            @Override
            public void run() {
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                mBillingClient.querySkuDetailsAsync(params.build(),
                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                                if (responseCode == BillingClient.BillingResponse.OK
                                        && skuDetailsList != null) {
                                    mSkuDetails = new HashMap<>();
                                    for (SkuDetails skuDetails : skuDetailsList) {
                                        mSkuDetails.put(skuDetails.getSku().toLowerCase(Locale.ROOT), skuDetails);
                                    }
                                }
                            }
                        });
            }
        };
        executeServiceRequest(queryRequest);
    }

    public void doPurchase(final String skuId) {
        Runnable purchaseFlowRequest = new Runnable() {
            @Override
            public void run() {

                BillingFlowParams purchaseParams = BillingFlowParams.newBuilder()
                        .setSku(skuId)
                        .setType(BillingClient.SkuType.INAPP)
                        .build();
                mBillingClient.launchBillingFlow(Game.instance(), purchaseParams);
            }
        };

        executeServiceRequest(purchaseFlowRequest);
    }

    @Override
    public void onPurchasesUpdated(@BillingClient.BillingResponse int responseCode, List<Purchase> purchases) {
        if (responseCode == BillingClient.BillingResponse.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (!verifySignature(purchase.getOriginalJson(), purchase.getSignature())) {
            EventCollector.logEvent("GoogleIap", "bad signature");
            return;
        }

        mPurchases.put(purchase.getSku().toLowerCase(Locale.ROOT), purchase);
    }


    private void onQueryPurchasesFinished(Purchase.PurchasesResult result) {
        // Have we been disposed of in the meantime? If so, or bad result code, then quit
        if (mBillingClient == null || result.getResponseCode() != BillingClient.BillingResponse.OK) {
            EventCollector.logEvent("google play billing", "queryPurchases", Integer.toString(result.getResponseCode()));
            return;
        }

        onPurchasesUpdated(BillingClient.BillingResponse.OK, result.getPurchasesList());
        mPurchasesUpdatedListener.onPurchasesUpdated();
    }

    public void queryPurchases() {
        Runnable queryToExecute = new Runnable() {
            @Override
            public void run() {
                Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);

                onQueryPurchasesFinished(purchasesResult);
            }
        };

        executeServiceRequest(queryToExecute);
    }

    private void startServiceConnection(final Runnable executeOnSuccess) {
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                EventCollector.logEvent("google play billing", Integer.toString(billingResponseCode));

                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    mIsServiceConnected = true;
                    if (executeOnSuccess != null) {
                        executeOnSuccess.run();
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                mIsServiceConnected = false;
            }
        });
    }

    private void executeServiceRequest(Runnable runnable) {
        if (isServiceConnected()) {
            runnable.run();
        } else {
            startServiceConnection(runnable);
        }
    }

    private boolean verifySignature(String signedData, String signature) {
        try {
            return GoogleIapCheck.verifyPurchase(Game.getVar(R.string.iapKey), signedData, signature);
        } catch (IOException e) {
            EventCollector.logException(e, "GoogleIap.verifySignature");
            return false;
        }
    }

    public boolean checkPurchase(String item) {
        return mPurchases.containsKey(item.toLowerCase(Locale.ROOT));
    }

    public String getSkuPrice(String sku) {
        String skuLowerCase = sku.toLowerCase(Locale.ROOT);
        if(mSkuDetails.containsKey(skuLowerCase)) {
            return mSkuDetails.get(skuLowerCase).getPrice();
        } else {
            EventCollector.logEvent("GoogleIap","no sku", sku);
            return "N/A";
        }
    }

    public boolean isServiceConnected() {
        return mIsServiceConnected;
    }
}

