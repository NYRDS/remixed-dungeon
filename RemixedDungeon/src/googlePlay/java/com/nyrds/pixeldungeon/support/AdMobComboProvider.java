package com.nyrds.pixeldungeon.support;

import android.annotation.SuppressLint;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;

public class AdMobComboProvider implements AdsUtilsCommon.IInterstitialProvider, AdsUtilsCommon.IBannerProvider {
    private static InterstitialAd mInterstitialAd = null;
    private AdView adView;

    AdMobComboProvider() {
        Game.instance().runOnUiThread(this::requestNewInterstitial);
    }

    private void requestNewInterstitial() {

        if (mInterstitialAd!=null) {
            return;
        }

        InterstitialAd.load(
                Game.instance(),
                Game.getVar(R.string.saveLoadAdUnitId),
                AdMob.makeAdRequest(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    }
                }
        );
    }

    @Override
    public void showInterstitial(final InterstitialPoint ret) {
        Game.instance().runOnUiThread(() -> {
            if (mInterstitialAd == null) {
                AdsUtilsCommon.interstitialFailed(AdMobComboProvider.this, ret);
                return;
            }

            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    mInterstitialAd = null;
                    requestNewInterstitial();
                    ret.returnToWork(true);
                }
            };

            mInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
            mInterstitialAd.show(Game.instance());
        });

    }

    @SuppressLint("MissingPermission")
    @Override
    public void displayBanner() {
        adView = new AdView(Game.instance());
        adView.setAdUnitId(Game.getVar(R.string.easyModeAdUnitId));
        adView.setBackgroundColor(Color.TRANSPARENT);
        adView.setAdListener(new AdmobBannerListener());
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.loadAd(AdMob.makeAdRequest());
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
            AdsUtilsCommon.bannerFailed(AdMobComboProvider.this);
        }
    }
}
