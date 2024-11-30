package com.nyrds.pixeldungeon.support;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.nyrds.platform.EventCollector;
import com.watabou.noosa.InterstitialPoint;
import com.nyrds.platform.game.RemixedDungeon;

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
        }

        @Override
        public void onInterstitialFailedToLoad() {
            //EventCollector.logException("appodeal load error");
            AdsUtilsCommon.interstitialFailed(AppodealInterstitialProvider.this, returnTo);
        }

        @Override
        public void onInterstitialShown() {
        }

        @Override
        public void onInterstitialShowFailed() {
            //EventCollector.logException("appodeal show error");
            AdsUtilsCommon.interstitialFailed(AppodealInterstitialProvider.this, returnTo);
        }

        @Override
        public void onInterstitialClicked() {
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
