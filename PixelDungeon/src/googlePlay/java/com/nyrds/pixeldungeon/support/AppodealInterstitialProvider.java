package com.nyrds.pixeldungeon.support;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.PixelDungeon;

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
            AdsUtilsCommon.interstitialFailed(AppodealInterstitialProvider.this, returnTo);
            return;
        }

        if(!Appodeal.show(PixelDungeon.instance(), Appodeal.INTERSTITIAL)) {
            AdsUtilsCommon.interstitialFailed(AppodealInterstitialProvider.this, returnTo);
        }
    }

    private class AppodealInterstitialCallbacks implements InterstitialCallbacks {
        @Override
        public void onInterstitialLoaded(boolean b) {
        }

        @Override
        public void onInterstitialFailedToLoad() {
            EventCollector.logException("appodeal_error");
            AdsUtilsCommon.interstitialFailed(AppodealInterstitialProvider.this, returnTo);
        }

        @Override
        public void onInterstitialShown() {
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
