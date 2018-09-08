package com.nyrds.pixeldungeon.support;

import android.graphics.Color;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;

class AdMobBannerProvider implements AdsUtilsCommon.IBannerProvider {

    @Override
    public void displayBanner() {

        AdView adView = new AdView(Game.instance());
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId(Game.getVar(R.string.easyModeAdUnitId));
        adView.setBackgroundColor(Color.TRANSPARENT);
        adView.setAdListener(new AdmobBannerListener());

        Game.instance().getLayout().addView(adView, 0);
        adView.loadAd(AdMob.makeAdRequest());


        Game.setNeedSceneRestart(true);
    }

    private class AdmobBannerListener extends AdListener {

        public void onAdFailedToLoad(int result) {
            EventCollector.logEvent("banner", "admob_no_banner", Integer.toString(result));
            AdsUtilsCommon.bannerFailed(AdMobBannerProvider.this);
        }
    }

}
