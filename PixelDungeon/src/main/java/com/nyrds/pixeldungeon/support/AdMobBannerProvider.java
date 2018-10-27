package com.nyrds.pixeldungeon.support;

import android.graphics.Color;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;

class AdMobBannerProvider implements AdsUtilsCommon.IBannerProvider {

    private AdView adView;

    @Override
    public void displayBanner() {

        adView = new AdView(Game.instance());
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId(Game.getVar(R.string.easyModeAdUnitId));
        adView.setBackgroundColor(Color.TRANSPARENT);
        adView.setAdListener(new AdmobBannerListener());

        adView.loadAd(AdMob.makeAdRequest());
    }

    private class AdmobBannerListener extends AdListener {

        @Override
        public void onAdLoaded() {
            AdsUtils.updateBanner(adView);
        }

        public void onAdFailedToLoad(int result) {
            EventCollector.logEvent("banner", "admob_no_banner", Integer.toString(result));
            AdsUtilsCommon.bannerFailed(AdMobBannerProvider.this);
        }
    }

}
