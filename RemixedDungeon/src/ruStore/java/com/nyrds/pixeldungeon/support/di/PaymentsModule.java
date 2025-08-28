package com.nyrds.pixeldungeon.support.di;

import android.app.Application;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.util.StringsManager;

import ru.rustore.sdk.billingclient.RuStoreBillingClient;
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory;

public class PaymentsModule {

    private static RuStoreBillingClient ruStoreBillingClient;

    public static void install(Application app) {
        String appId = StringsManager.getVar(R.string.rustore_app_id);
        String deeplinkScheme = StringsManager.getVar(R.string.rustore_deeplink_scheme);
        
        ruStoreBillingClient = RuStoreBillingClientFactory.INSTANCE.create(
                app,
                appId,
                deeplinkScheme,
                null, // themeProvider
                null, // externalPaymentLoggerFactory
                false // debugLogs - set to true for development
        );
    }

    public static RuStoreBillingClient provideRuStorebillingClient() {
        if(ruStoreBillingClient == null) {
            install(RemixedDungeonApp.getApp());
        }
        return ruStoreBillingClient;
    }

}
