package com.nyrds.pixeldungeon.support;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.InterstitialPoint;

public class AdMobInterstitialProvider implements AdsUtilsCommon.IInterstitialProvider {
    private static InterstitialAd mInterstitialAd = null;

    AdMobInterstitialProvider() {
        Game.runOnMainThread(this::requestNewInterstitial);
    }

    private void requestNewInterstitial() {

        if (mInterstitialAd!=null) {
            return;
        }

        InterstitialAd.load(
                Game.instance(),
                ModdingMode.getInterstitialId(),
                AdMob.makeAdRequest(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        EventCollector.logEvent("Interstitial failed", loadAdError.toString());
                    }
                }
        );
    }

    @Override
    public void showInterstitial(final InterstitialPoint ret) {
        Game.runOnMainThread(() -> {
            if (mInterstitialAd == null) {
                AdsUtilsCommon.interstitialFailed(AdMobInterstitialProvider.this, ret);
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

    @Override
    public boolean isReady() {
        return mInterstitialAd!=null;
    }

}
