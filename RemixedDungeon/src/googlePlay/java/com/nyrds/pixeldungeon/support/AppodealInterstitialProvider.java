package com.nyrds.pixeldungeon.support;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.RemixedDungeon;
import com.watabou.noosa.InterstitialPoint;

class AppodealInterstitialProvider implements AdsUtilsCommon.IInterstitialProvider {

    private InterstitialPoint returnTo;

    @Override
    public void showInterstitial(InterstitialPoint ret) {
        AppodealAdapter.init();

        returnTo = ret;

        Appodeal.setInterstitialCallbacks(new AppodealInterstitialCallbacks());

        if(!Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
            AdsUtilsCommon.interstitialFailed(AppodealInterstitialProvider.this, returnTo);
            return;
        }

        if(!Appodeal.show(RemixedDungeon.instance(), Appodeal.INTERSTITIAL)) {
            AdsUtilsCommon.interstitialFailed(AppodealInterstitialProvider.this, returnTo);
        }
    }

    @Override
    public boolean isReady() {
        return Appodeal.isLoaded(Appodeal.INTERSTITIAL);
    }

    private class AppodealInterstitialCallbacks implements InterstitialCallbacks {
        @Override
        public void onInterstitialLoaded(boolean b) {
            EventCollector.logEvent("appodeal_interstitial_loaded");
        }

        @Override
        public void onInterstitialFailedToLoad() {
            EventCollector.logEvent("appodeal_interstitial_load_error");
            AdsUtilsCommon.interstitialFailed(AppodealInterstitialProvider.this, returnTo);
        }

        @Override
        public void onInterstitialShown() {
            EventCollector.logEvent("appodeal_interstitial_shown");
        }

        @Override
        public void onInterstitialShowFailed() {
            EventCollector.logEvent("appodeal_interstitial_show_error");
            AdsUtilsCommon.interstitialFailed(AppodealInterstitialProvider.this, returnTo);
        }

        @Override
        public void onInterstitialClicked() {
            EventCollector.logEvent("appodeal_interstitial_clicked");
        }

        @Override
        public void onInterstitialClosed() {
            returnTo.returnToWork(true);
        }

        @Override
        public void onInterstitialExpired() {
        }
    }
}
