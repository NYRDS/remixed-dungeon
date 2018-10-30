package com.nyrds.pixeldungeon.support;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;

public class AdMobInterstitialProvider implements AdsUtilsCommon.IInterstitialProvider {
    private static InterstitialAd mInterstitialAd;

    static {
        Game.instance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mInterstitialAd == null) {
                    mInterstitialAd = new InterstitialAd(Game.instance());
                    mInterstitialAd.setAdUnitId(Game.getVar(R.string.saveLoadAdUnitId));
                    requestNewInterstitial();
                }
            }
        });
    }


    private static void requestNewInterstitial() {

        if (mInterstitialAd.isLoaded() || mInterstitialAd.isLoading()) {
            return;
        }

        mInterstitialAd.setAdListener(new AdMobAdListener());

        mInterstitialAd.loadAd(AdMob.makeAdRequest());
    }


    @Override
    public void showInterstitial(final InterstitialPoint ret) {
        Game.instance().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mInterstitialAd == null) {
                    EventCollector.logEvent("interstitial", "admob","mInterstitialAd == null");
                    AdsUtilsCommon.interstitialFailed(AdMobInterstitialProvider.this, ret);
                    return;
                }

                if (!mInterstitialAd.isLoaded()) {
                    EventCollector.logEvent("interstitial", "admob","not loaded");
                    AdsUtilsCommon.interstitialFailed(AdMobInterstitialProvider.this, ret);
                    return;
                }

                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        requestNewInterstitial();
                        ret.returnToWork(true);
                    }
                });
                mInterstitialAd.show();
            }
        });

    }

    private static class AdMobAdListener extends AdListener {
        @Override
        public void onAdClosed() {
            super.onAdClosed();
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            super.onAdFailedToLoad(errorCode);
            EventCollector.logEvent("interstitial", "admob_error", Integer.toString(errorCode));
        }

        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
        }

        @Override
        public void onAdOpened() {
            super.onAdOpened();
        }

        @Override
        public void onAdLeftApplication() {
            super.onAdLeftApplication();
        }
    }
}
