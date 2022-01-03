package com.nyrds.pixeldungeon.support;

import android.annotation.SuppressLint;
import android.graphics.Color;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.util.StringsManager;

public class AdMobBannerProvider implements  AdsUtilsCommon.IBannerProvider {
    private AdView adView;

    private boolean loaded = true;

    AdMobBannerProvider(){ }

    @SuppressLint("MissingPermission")
    @Override
    public void displayBanner() {
        adView = new AdView(Game.instance());
        adView.setAdUnitId(StringsManager.getVar(R.string.easyModeAdUnitId));
        adView.setBackgroundColor(Color.TRANSPARENT);
        adView.setAdListener(new AdMobBannerListener());
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.loadAd(AdMob.makeAdRequest());
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
        }

        @Override
        public void onAdFailedToLoad(LoadAdError reason) {
            EventCollector.logEvent("Banner failed", reason.toString());
            loaded = false;
            AdsUtilsCommon.bannerFailed(AdMobBannerProvider.this);
        }
    }
}
