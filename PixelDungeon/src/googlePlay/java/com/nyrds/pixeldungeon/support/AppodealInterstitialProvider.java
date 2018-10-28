package com.nyrds.pixeldungeon.support;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.utils.GLog;

class AppodealInterstitialProvider implements AdsUtilsCommon.IInterstitialProvider {

    private InterstitialPoint returnTo;

    static {
        AppodealAdapter.init();
    }

    @Override
    public void showInterstitial(InterstitialPoint ret) {
        returnTo = ret;

        Appodeal.setInterstitialCallbacks(new AppodealInterstitialCallbacks());

        if(!Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
            //GLog.w("not loaded");
            AdsUtilsCommon.interstitialFailed(AppodealInterstitialProvider.this, returnTo);
            return;
        }

        if(!Appodeal.show(PixelDungeon.instance(), Appodeal.INTERSTITIAL)) {
            //GLog.w("not shown");
            AdsUtilsCommon.interstitialFailed(AppodealInterstitialProvider.this, returnTo);
        }
    }

    private class AppodealInterstitialCallbacks implements InterstitialCallbacks {
        @Override
        public void onInterstitialLoaded(boolean b) {
            GLog.w("%s %b","loaded",b);
        }

        @Override
        public void onInterstitialFailedToLoad() {
            //GLog.w("%s","failed");
            EventCollector.logEvent("interstitial", "appodeal_error");
            AdsUtilsCommon.interstitialFailed(AppodealInterstitialProvider.this, returnTo);
        }

        @Override
        public void onInterstitialShown() {
            GLog.w("%s","shown");
        }

        @Override
        public void onInterstitialClicked() {
            GLog.w("%s","clicked");
        }

        @Override
        public void onInterstitialClosed() {
            //GLog.w("%s","closed");
            returnTo.returnToWork(true);
        }

        @Override
        public void onInterstitialExpired() {
        }
    }
}
