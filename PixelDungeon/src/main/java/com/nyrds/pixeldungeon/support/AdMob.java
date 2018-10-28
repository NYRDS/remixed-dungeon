package com.nyrds.pixeldungeon.support;

import android.os.Bundle;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.nyrds.android.util.Flavours;
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;

class AdMob {
    private static boolean mAdLoadInProgress;
    private static InterstitialAd mInterstitialAd;

    public static AdRequest makeAdRequest() {
        if (EuConsent.getConsentLevel() < EuConsent.PERSONALIZED) {
            Bundle extras = new Bundle();
            extras.putString("npa", "1");

            return new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();
        }

        return new AdRequest.Builder().build();
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
        mInterstitialAd.loadAd(makeAdRequest());
    }

    public static void initInterstitial() {
        if (googleAdsUsable() && Util.isConnectedToInternet()) {
            {
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
        }
    }

    public static boolean googleAdsUsable() {
        return Flavours.haveAds();
    }

    static void displayIsAd(final InterstitialPoint work) {
        if (googleAdsUsable() && Util.isConnectedToInternet()) {
            Game.instance().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mInterstitialAd == null) {
                        work.returnToWork(false);
                        return;
                    }

                    if (!mInterstitialAd.isLoaded()) {
                        work.returnToWork(false);
                        return;
                    }

                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            requestNewInterstitial();
                            work.returnToWork(true);
                        }
                    });
                    mInterstitialAd.show();
                }
            });
        } else {
            OfflineAds.displayIsAd(work);
        }
    }
}
