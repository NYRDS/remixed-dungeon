package com.nyrds.pixeldungeon.support;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;

public class AdMobInterstitialProvider implements AdsUtilsCommon.IInterstitialProvider {
    private static boolean mAdLoadInProgress;
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

        if (mAdLoadInProgress) {
            return;
        }

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                mAdLoadInProgress = false;
                EventCollector.logEvent("interstitial", "admob_error", Integer.toString(errorCode));
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdLoadInProgress = false;
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }
        });

        mAdLoadInProgress = true;
        mInterstitialAd.loadAd(AdMob.makeAdRequest());
    }


    @Override
    public void showInterstitial(final InterstitialPoint ret) {
        Game.instance().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mInterstitialAd == null) {
                    AdsUtilsCommon.interstitialFailed(AdMobInterstitialProvider.this, ret);
                    return;
                }

                if (!mInterstitialAd.isLoaded()) {
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
}
