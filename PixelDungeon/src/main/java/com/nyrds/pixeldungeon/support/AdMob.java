package com.nyrds.pixeldungeon.support;

import android.os.Bundle;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.nyrds.android.util.Flavours;
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;

import java.util.HashMap;
import java.util.Map;

class AdMob {
    private static Map<InterstitialAd, Boolean> mAdLoadInProgress = new HashMap<>();

    static InterstitialAd mSaveAndLoadAd;
    static InterstitialAd mEasyModeSmallScreenAd;

    public static AdRequest makeAdRequest() {
        if (EuConsent.getConsentLevel() < EuConsent.PERSONALIZED) {
            Bundle extras = new Bundle();
            extras.putString("npa", "1");

            return new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();
        }

        return new AdRequest.Builder().build();
    }

    private static void requestNewInterstitial(final InterstitialAd isAd) {

        Boolean loadAlreadyInProgress = mAdLoadInProgress.get(isAd);

        if (loadAlreadyInProgress != null && loadAlreadyInProgress) {
            return;
        }

        isAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                mAdLoadInProgress.put(isAd, false);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdLoadInProgress.put(isAd, false);
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

        mAdLoadInProgress.put(isAd, true);
        isAd.loadAd(makeAdRequest());
    }

    static void initEasyModeIntersitial() {
        if (googleAdsUsable() && Util.isConnectedToInternet()) {
            {
                Game.instance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mEasyModeSmallScreenAd == null) {
                            mEasyModeSmallScreenAd = new InterstitialAd(Game.instance());
                            mEasyModeSmallScreenAd.setAdUnitId(Game.getVar(R.string.easyModeSmallScreenAdUnitId));
                            requestNewInterstitial(mEasyModeSmallScreenAd);
                        }
                    }
                });
            }
        }
    }

    public static void initSaveAndLoadIntersitial() {
        if (googleAdsUsable() && Util.isConnectedToInternet()) {
            {
                Game.instance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSaveAndLoadAd == null) {
                            mSaveAndLoadAd = new InterstitialAd(Game.instance());
                            mSaveAndLoadAd.setAdUnitId(Game.getVar(R.string.saveLoadAdUnitId));
                            requestNewInterstitial(mSaveAndLoadAd);
                        }
                    }
                });
            }
        }
    }

    public static boolean googleAdsUsable() {
        return Flavours.haveAds();
    }

    static void displayIsAd(final InterstitialPoint work, final InterstitialAd isAd) {
        if (googleAdsUsable() && Util.isConnectedToInternet()) {
            Game.instance().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (isAd == null) {
                        work.returnToWork(false);
                        return;
                    }

                    if (!isAd.isLoaded()) {
                        work.returnToWork(false);
                        return;
                    }

                    isAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            requestNewInterstitial(isAd);
                            work.returnToWork(true);
                        }
                    });
                    isAd.show();
                }
            });
        } else {
            OfflineAds.displayIsAd(work);
        }
    }
}
