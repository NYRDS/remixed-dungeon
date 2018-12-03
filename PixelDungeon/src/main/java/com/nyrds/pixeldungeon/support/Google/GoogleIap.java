package com.nyrds.pixeldungeon.support.Google;

import android.content.Context;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.IPurchasesUpdated;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class GoogleIap implements PurchasesUpdatedListener, PurchaseHistoryResponseListener, ConsumeResponseListener {

    private final Map<String, Purchase> mPurchases = new HashMap<>();
    private BillingClient mBillingClient;
    private boolean mIsServiceConnected;
    private IPurchasesUpdated mPurchasesUpdatedListener;
    private Map<String, SkuDetails> mSkuDetails = new HashMap<>();
    private boolean mIsServiceConnecting;

    private ConcurrentLinkedQueue<Runnable> mRequests = new ConcurrentLinkedQueue<>();

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
            mPurchasesUpdatedListener.onPurchasesUpdated();
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (!verifySignature(purchase.getOriginalJson(), purchase.getSignature())) {
            EventCollector.logException("bad signature");
            return;
        }
        //GLog.w("purchase: %s",purchase.toString());
        //mBillingClient.consumeAsync(purchase.getPurchaseToken(),this);
        mPurchases.put(purchase.getSku().toLowerCase(Locale.ROOT), purchase);
    }

    public void queryPurchases() {
        Runnable queryToExecute = new Runnable() {
            @Override
            public void run() {
                Purchase.PurchasesResult result = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
                onPurchasesUpdated(result.getResponseCode(), result.getPurchasesList());

                //mBillingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, GoogleIap.this);
            }
        };

        executeServiceRequest(queryToExecute);
    }

    private void startServiceConnection() {
        if(mIsServiceConnecting) {
            return;
        }

        mIsServiceConnecting = true;
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {

                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    mIsServiceConnected = true;
                    for (Runnable runnable:mRequests) {
                        getExecutor().execute(runnable);
                    }
                }   else {
                    EventCollector.logException("google play billing" + Integer.toString(billingResponseCode));
                }
                mIsServiceConnecting = false;
            }

            @Override
            public void onBillingServiceDisconnected() {
                mIsServiceConnected = false;
                mIsServiceConnecting = false;
            }
        });
    }

    private void executeServiceRequest(final Runnable runnable) {
        Game.instance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isServiceConnected()) {
                    getExecutor().execute(runnable);
                } else {
                    mRequests.add(runnable);
                    startServiceConnection();
                }
            }
        });

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
        if (mSkuDetails.containsKey(skuLowerCase)) {
            return mSkuDetails.get(skuLowerCase).getPrice();
        } else {
            EventCollector.logException(sku);
            return "N/A";
        }
    }

    private Executor getExecutor() {
        return Game.instance().executor;
    }

    public boolean isServiceConnected() {
        return mIsServiceConnected && !mIsServiceConnecting;
    }

    @Override
    public void onPurchaseHistoryResponse(int responseCode, List<Purchase> purchasesList) {
        if (responseCode != BillingClient.BillingResponse.OK) {
            EventCollector.logException("queryPurchasesHistory" + Integer.toString(responseCode));
        }
    }

    @Override
    public void onConsumeResponse(int responseCode, String purchaseToken) {
        GLog.w("consumed: %d %s", responseCode, purchaseToken);
    }
}