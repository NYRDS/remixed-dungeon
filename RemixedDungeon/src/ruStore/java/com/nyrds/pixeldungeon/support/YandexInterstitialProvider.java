package com.nyrds.pixeldungeon.support;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.watabou.noosa.InterstitialPoint;
import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.AdRequestConfiguration;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader;

public class YandexInterstitialProvider implements AdsUtilsCommon.IInterstitialProvider {
    @Nullable
    private InterstitialAd mInterstitialAd = null;
    @Nullable
    private InterstitialAdLoader mInterstitialAdLoader = null;

    private final String adId;

    YandexInterstitialProvider(String id) {
        adId = id;
        GameLoop.runOnMainThread(this::requestNewInterstitial);
    }

    private void requestNewInterstitial() {
        if (mInterstitialAd!=null) {
            return;
        }

        // Interstitial ads loading should occur after initialization of the SDK.
        // Initialize SDK as early as possible, for example in Application.onCreate or Activity.onCreate
        mInterstitialAdLoader = new InterstitialAdLoader(Game.instance());
        mInterstitialAdLoader.setAdLoadListener(new InterstitialAdLoadListener() {
            @Override
            public void onAdLoaded(@NonNull final InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                // The ad was loaded successfully. You can now show the ad.
            }

            @Override
            public void onAdFailedToLoad(@NonNull final AdRequestError adRequestError) {
                // Ad failed to load with AdRequestError.
                // Attempting to load a new ad from the onAdFailedToLoad() method is strongly discouraged.
                EventCollector.logEvent("Interstitial failed", adRequestError.toString());
            }
        });
        loadInterstitialAd();
    }


    private void loadInterstitialAd() {
        if (mInterstitialAdLoader != null ) {
            final AdRequestConfiguration adRequestConfiguration =
                    new AdRequestConfiguration.Builder(adId).build();
            mInterstitialAdLoader.loadAd(adRequestConfiguration);
        }
    }

    @Override
    public void showInterstitial(final InterstitialPoint ret) {
        GameLoop.runOnMainThread(() -> {
            if (mInterstitialAd == null) {
                AdsUtilsCommon.interstitialFailed(YandexInterstitialProvider.this, ret);
                return;
            }
            mInterstitialAd.setAdEventListener(new InterstitialAdEventListener() {
                @Override
                public void onAdShown() {
                    // Called when ad is shown.
                }

                @Override
                public void onAdFailedToShow(@NonNull final AdError adError) {
                    // Called when an InterstitialAd failed to show.
                }

                @Override
                public void onAdDismissed() {
                    // Called when an ad is dismissed.
                    // Clean resources after Ad dismissed
                    if (mInterstitialAd != null) {
                        mInterstitialAd.setAdEventListener(null);
                        mInterstitialAd = null;
                    }

                    // Now you can preload the next interstitial ad.
                    requestNewInterstitial();
                    ret.returnToWork(true);
                }

                @Override
                public void onAdClicked() {
                    // Called when a click is recorded for an ad.
                }

                @Override
                public void onAdImpression(@Nullable final ImpressionData impressionData) {
                    // Called when an impression is recorded for an ad.
                }
            });

            mInterstitialAd.show(Game.instance());
        });

    }

    @Override
    public boolean isReady() {
        return mInterstitialAd!=null;
    }

}
