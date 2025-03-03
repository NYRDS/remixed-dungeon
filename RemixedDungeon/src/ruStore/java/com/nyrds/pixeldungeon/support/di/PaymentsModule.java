package com.nyrds.pixeldungeon.support.di;

import android.app.Application;

import com.nyrds.platform.app.RemixedDungeonApp;

import ru.rustore.sdk.billingclient.RuStoreBillingClient;
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory;

public class PaymentsModule {

    private static RuStoreBillingClient ruStoreBillingClient;

    public static void install(Application app) {
        ruStoreBillingClient = RuStoreBillingClientFactory.INSTANCE.create(
                app,
                "184050",
                "rustoresdkexamplescheme",
                null,
                null,
                true
        );
    }

    public static RuStoreBillingClient provideRuStorebillingClient() {
        if(ruStoreBillingClient == null) {
            install(RemixedDungeonApp.getApp());
        }
        return ruStoreBillingClient;
    }

}
