package com.nyrds.market;

import android.os.Bundle;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.RemoteConfig;
import com.nyrds.platform.app.AppIntegrity;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.utils.GLog;

public class MarketApp {
    static public void init(RemixedDungeonApp app) {
        FirebaseApp.initializeApp(app);
        EventCollector.init();
        RemoteConfig.getInstance(app);
    }

    // runs unconditionally from Application.onCreate - repacked installs are exactly
    // the ones checkOwnSignature() would silence, so they must still self-label
    static public void integrityReport(RemixedDungeonApp app) {
        try {
            AppIntegrity.Report report = AppIntegrity.report();

            // rides every crash report from this install
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey("repacked", report.repacked);
            crashlytics.setCustomKey("packer", report.packer);
            crashlytics.setCustomKey("installer", report.installer);
            crashlytics.setCustomKey("sig_sha256", report.sigSha256);

            // population telemetry, same consent gate as the rest of analytics
            if (analyticsUsable()) {
                Bundle params = new Bundle();
                params.putBoolean("repacked", report.repacked);
                params.putString("packer", report.packer);
                params.putString("installer", report.installer);
                FirebaseAnalytics.getInstance(app).logEvent("app_integrity", params);
            }
        } catch (Throwable e) {
            GLog.w("integrityReport failed: %s", e.getMessage());
        }
    }

    static private boolean analyticsUsable() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_COLLECT_STATS, 1) > 0 && !Util.isDebug();
    }
}
