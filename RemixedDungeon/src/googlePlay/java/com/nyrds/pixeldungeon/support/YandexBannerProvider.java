package com.nyrds.pixeldungeon.support;

import android.annotation.SuppressLint;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;

public class YandexBannerProvider implements AdsUtilsCommon.IBannerProvider {
    private BannerAdView adView;

    private boolean loaded = true;
    private final String adId;

    YandexBannerProvider(String id){
        adId = id;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void displayBanner() {
        adView = new BannerAdView(Game.instance());
        adView.setAdUnitId(adId);
        adView.setBackgroundColor(Color.TRANSPARENT);
        adView.setAdUnitId(adId);
        adView.setBannerAdEventListener(new YandexBannerListener());
        //adView.setAdSize(BannerAdView.);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public boolean isReady() {
        return loaded;
    }

    private class YandexBannerListener implements BannerAdEventListener {

        @Override
        public void onAdLoaded() {
            Ads.updateBanner(adView);
            loaded = true;
        }

        @Override
        public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
            EventCollector.logEvent("Banner failed", adRequestError.toString());
            loaded = false;
            AdsUtilsCommon.bannerFailed(YandexBannerProvider.this);
        }

        @Override
        public void onLeftApplication() {

        }

        @Override
        public void onReturnedToApplication() {

        }

        @Override
        public void onImpression(@Nullable ImpressionData impressionData) {

        }

        @Override
        public void onAdClicked() {

        }
    }
}
