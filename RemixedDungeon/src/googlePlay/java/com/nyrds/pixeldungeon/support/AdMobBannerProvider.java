package com.nyrds.pixeldungeon.support;

import android.graphics.Color;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;

public class AdMobBannerProvider implements  AdsUtilsCommon.IBannerProvider {
    private AdView adView;

    private boolean loaded = true;
    private final String adId;

    AdMobBannerProvider(String id){
        adId = id;
    }


    @Override
    public void displayBanner() {
        adView = new AdView(Game.instance());
        adView.setAdUnitId(adId);
        adView.setBackgroundColor(Color.TRANSPARENT);
        adView.setAdListener(new AdMobBannerListener());
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.loadAd(AdMob.makeAdRequest());
        EventCollector.logEvent("admob_banner_requested");
    }

    @Override
    public boolean isReady() {
        return loaded;
    }

    private class AdMobBannerListener extends AdListener {

        @Override
        public void onAdLoaded() {
            Ads.updateBanner(adView);
            loaded = true;
            EventCollector.logEvent("admob_banner_loaded");
        }

        @Override
        public void onAdImpression() {
            super.onAdImpression();
            EventCollector.logEvent("admob_banner_shown");
        }

        @Override
        public void onAdFailedToLoad(LoadAdError reason) {
            EventCollector.logEvent("admob_banner_failed", reason.toString());
            loaded = false;
            AdsUtilsCommon.bannerFailed(AdMobBannerProvider.this);
        }
    }
}
