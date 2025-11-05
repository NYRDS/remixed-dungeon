package com.nyrds.pixeldungeon.support;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.nyrds.market.GoogleIapCheck;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.platform.support.IPurchasesUpdated;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class IapAdapter implements PurchasesUpdatedListener, ConsumeResponseListener {

    private final Map<String, Purchase> mPurchases = new HashMap<>();
    private final BillingClient mBillingClient;
    private boolean mIsServiceConnected;
    private final IPurchasesUpdated mPurchasesUpdatedListener;
    private Map<String, ProductDetails> mSkuDetails = new HashMap<>();
    private boolean mIsServiceConnecting;

    private final ConcurrentLinkedQueue<Runnable> mRequests = new ConcurrentLinkedQueue<>();

    public IapAdapter(Context context, IPurchasesUpdated purchasesUpdatedListener) {
        mBillingClient = BillingClient.newBuilder(context)
                .enableAutoServiceReconnection()
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .setListener(this)
                .build();
        mPurchasesUpdatedListener = purchasesUpdatedListener;
    }

    public void querySkuList(final List<String> skuList) {
        Runnable queryRequest = () -> {
            List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
            for (String sku : skuList) {
                productList.add(QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(sku)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build());
            }
            QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build();
            mBillingClient.queryProductDetailsAsync(params,
                    (billingResult, queryProductDetailsResult) -> {
                        List<ProductDetails> list = queryProductDetailsResult.getProductDetailsList();
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            mSkuDetails = new HashMap<>();
                            for (ProductDetails productDetails : list) {
                                mSkuDetails.put(productDetails.getProductId().toLowerCase(Locale.ROOT), productDetails);
                            }
                        }
                    });
        };
        executeServiceRequest(queryRequest);
    }

    public void doPurchase(final String skuId) {
        if (mSkuDetails.containsKey(skuId.toLowerCase(Locale.ROOT))) {
            ProductDetails productDetails = mSkuDetails.get(skuId.toLowerCase(Locale.ROOT));
            String offerToken = "";
            List<ProductDetails.OneTimePurchaseOfferDetails> offerDetailsList = productDetails.getOneTimePurchaseOfferDetailsList();
            if (!offerDetailsList.isEmpty()) {
                offerToken = offerDetailsList.get(0).getOfferToken();
            }

            String finalOfferToken = offerToken;
            Runnable purchaseFlowRequest = () -> {
                try {
                    List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
                    productDetailsParamsList.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(finalOfferToken)
                            .build());

                    BillingFlowParams purchaseParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build();
                    mBillingClient.launchBillingFlow(Game.instance(), purchaseParams);
                } catch (Throwable e) { // for backward compatibility
                    EventCollector.logException(e, "GoogleIap.doPurchase");
                }
            };

            executeServiceRequest(purchaseFlowRequest);
        } else {
            EventCollector.logException("No sku: |" + skuId + "|");
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (!verifySignature(purchase.getOriginalJson(), purchase.getSignature())) {
            EventCollector.logException("bad signature");
            return;
        }
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> EventCollector.logEvent("billing_result",
                        billingResult.getResponseCode()
                                + "->"
                                + billingResult.getDebugMessage()));
            }

            for (var product : purchase.getProducts()) {
                mPurchases.put(product.toLowerCase(Locale.ROOT), purchase);

                String orderId = purchase.getOrderId();
                String purchaseData = purchase.getOrderId() + ","
                        + purchase.getPackageName() + ","
                        + product + ","
                        + purchase.getPurchaseToken();
                if (!Preferences.INSTANCE.getBoolean(orderId, false)) {
                    EventCollector.logEvent("iap_data", purchaseData);
                    Preferences.INSTANCE.put(orderId, true);
                }
            }
        }
    }

    public void queryPurchases() {
        Runnable queryToExecute = () -> {
            QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build();
            mBillingClient.queryPurchasesAsync(params, new PurchasesResponseListener() {
                @Override
                public void onQueryPurchasesResponse(@NotNull BillingResult billingResult, @NotNull List<Purchase> purchases) {
                    onPurchasesUpdated(billingResult, purchases);
                }
            });
        };

        executeServiceRequest(queryToExecute);
    }

    private void startServiceConnection() {
        if (mIsServiceConnecting) {
            return;
        }

        mIsServiceConnecting = true;
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NotNull BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    mIsServiceConnected = true;
                    for (Runnable runnable : mRequests) {
                        getExecutor().execute(runnable);
                    }
                } else {
                    //EventCollector.logException("google play billing:" + billingResult.getDebugMessage());
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
        GameLoop.runOnMainThread(() -> {
            if (isServiceConnected()) {
                getExecutor().execute(runnable);
            } else {
                mRequests.add(runnable);
                startServiceConnection();
            }
        });
    }

    private boolean verifySignature(String signedData, String signature) {
        try {
            return GoogleIapCheck.verifyPurchase(StringsManager.getVar(R.string.iapKey), signedData, signature);
        } catch (IOException e) {
            EventCollector.logException(e, "GoogleIap.verifySignature");
            return false;
        }
    }

    public boolean checkPurchase(String item) {
        return mPurchases.containsKey(item.toLowerCase(Locale.ROOT));
    }

    @NotNull
    public String getSkuPrice(@NotNull String sku) {
        String skuLowerCase = sku.toLowerCase(Locale.ROOT);
        if (mSkuDetails.containsKey(skuLowerCase)) {
            ProductDetails productDetails = mSkuDetails.get(skuLowerCase);
            List<ProductDetails.OneTimePurchaseOfferDetails> offerDetailsList = productDetails.getOneTimePurchaseOfferDetailsList();
            if (!offerDetailsList.isEmpty()) {
                return offerDetailsList.get(0).getFormattedPrice();
            }
        }
        //EventCollector.logException(sku); //Yeah, this happens a lot...
        return Utils.EMPTY_STRING;
    }

    private Executor getExecutor() {
        return Game.instance().serviceExecutor;
    }

    public boolean isServiceConnected() {
        return mIsServiceConnected && !mIsServiceConnecting;
    }

    @Override
    public void onConsumeResponse(BillingResult billingResult, @NotNull String s) {
        GLog.w("consumed: %d %s", billingResult.getDebugMessage(), s);
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && list != null) {
            for (Purchase purchase : list) {
                handlePurchase(purchase);
            }
            mPurchasesUpdatedListener.onPurchasesUpdated();
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    public void onNewIntent(Intent intent) {
    }
}