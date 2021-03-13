package com.nyrds.pixeldungeon.support;

import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Display;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.RemixedDungeon;

public class AdMobComboProvider implements AdsUtilsCommon.IInterstitialProvider, AdsUtilsCommon.IBannerProvider {
    private static InterstitialAd mInterstitialAd;
    private AdView adView;

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
        Game.instance().runOnUiThread(() -> {
            if (mInterstitialAd == null) {
                EventCollector.logException("mInterstitialAd == null");
                AdsUtilsCommon.interstitialFailed(AdMobComboProvider.this, ret);
                return;
            }

            if (!mInterstitialAd.isLoaded()) {
                //EventCollector.logException("not loaded");
                AdsUtilsCommon.interstitialFailed(AdMobComboProvider.this, ret);
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
        });

    }

    @Override
    public void displayBanner() {
        adView = new AdView(Game.instance());
        adView.setAdUnitId(Game.getVar(R.string.easyModeAdUnitId));
        adView.setBackgroundColor(Color.TRANSPARENT);
        adView.setAdListener(new AdmobBannerListener());
        adView.setAdSize(getAdSize());
        adView.loadAd(AdMob.makeAdRequest());
    }

    private AdSize getAdSize() {
        Display display = RemixedDungeon.instance().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(RemixedDungeon.instance(), adWidth);
    }


    @Override
    public boolean isReady() {
        return true;
    }

    private class AdmobBannerListener extends AdListener {

        @Override
        public void onAdLoaded() {
            Ads.updateBanner(adView);
        }

        @Override
        public void onAdFailedToLoad(LoadAdError reason) {
            //EventCollector.logException( "admob banner:" + reason.toString());
            AdsUtilsCommon.bannerFailed(AdMobComboProvider.this);
        }
    }

    private static class AdMobAdListener extends AdListener {
        @Override
        public void onAdClosed() {
            super.onAdClosed();
        }

        @Override
        public void onAdFailedToLoad(LoadAdError reason) {
            //EventCollector.logException( "admob is:" + reason.toString());
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
