package com.nyrds.pixeldungeon.support;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.watabou.noosa.InterstitialPoint;

public class AdMobInterstitialProvider implements AdsUtilsCommon.IInterstitialProvider {
    private InterstitialAd mInterstitialAd = null;

    private final String adId;

    AdMobInterstitialProvider(String id) {
        adId = id;
        GameLoop.runOnMainThread(this::requestNewInterstitial);
    }

    private void requestNewInterstitial() {
        EventCollector.logEvent("admob_interstitial_requested");
        if (mInterstitialAd!=null) {
            return;
        }

        EventCollector.logEvent("admob_interstitial_load_attempt");
        InterstitialAd.load(
                Game.instance(),
                adId,
                AdMob.makeAdRequest(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        EventCollector.logEvent("admob_interstitial_loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        EventCollector.logEvent("admob_interstitial_failed", loadAdError.toString());
                    }
                }
        );
    }

    @Override
    public void showInterstitial(final InterstitialPoint ret) {
        GameLoop.runOnMainThread(() -> {
            if (mInterstitialAd == null) {
                AdsUtilsCommon.interstitialFailed(AdMobInterstitialProvider.this, ret);
                return;
            }

            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    EventCollector.logEvent("admob_interstitial_shown");
                    mInterstitialAd = null;
                    requestNewInterstitial();
                    ret.returnToWork(true);
                }
            };

            mInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
            mInterstitialAd.show(Game.instance());
        });

    }

    @Override
    public boolean isReady() {
        return mInterstitialAd!=null;
    }

}
