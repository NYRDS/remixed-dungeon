package com.nyrds.pixeldungeon.support;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.BannerView;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Game;

class AppodealBannerProvider implements AdsUtilsCommon.IBannerProvider {

    private BannerView adView;

    static {
        AppodealAdapter.init();
    }

    @Override
    public void displayBanner() {

        Appodeal.setBannerCallbacks(new AppodealBannerCallbacks());

        adView = Appodeal.getBannerView(Game.instance());

        if(!Appodeal.show(Game.instance(), Appodeal.BANNER_VIEW)){
            EventCollector.logException("appodeal_show_failed");
            AdsUtilsCommon.bannerFailed(AppodealBannerProvider.this);
            return;
        }
        AdsUtils.updateBanner(adView);
    }

    private class AppodealBannerCallbacks implements BannerCallbacks {
        @Override
        public void onBannerLoaded(int i, boolean b) {
            AdsUtils.updateBanner(adView);
        }

        @Override
        public void onBannerFailedToLoad() {
            EventCollector.logException("appodeal_no_banner");
            AdsUtilsCommon.bannerFailed(AppodealBannerProvider.this);
        }

        @Override
        public void onBannerShown() {

        }

        @Override
        public void onBannerClicked() {

        }

        @Override
        public void onBannerExpired() {

        }
    }
}
