package com.nyrds.pixeldungeon.support;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.BannerView;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;

import org.jetbrains.annotations.NotNull;

class AppodealBannerProvider implements AdsUtilsCommon.IBannerProvider {

    private BannerView adView;

    private static AppodealBannerProvider instance;

    private AppodealBannerProvider() { }

    @NotNull
    static public AppodealBannerProvider getInstance() {
        if(instance==null) {
            instance = new AppodealBannerProvider();
        }
        return instance;
    }

    @Override
    public void displayBanner() {
        AppodealAdapter.init();

        Appodeal.setBannerCallbacks(new AppodealBannerCallbacks());

        adView = Appodeal.getBannerView(Game.instance());

        if(!Appodeal.show(Game.instance(), Appodeal.BANNER_VIEW)){
            //EventCollector.logException("appodeal_show_failed");
            AdsUtilsCommon.bannerFailed(AppodealBannerProvider.this);
            return;
        }
        Ads.updateBanner(adView);
    }

    @Override
    public boolean isReady() {
        return Appodeal.isLoaded(Appodeal.BANNER);
    }

    private class AppodealBannerCallbacks implements BannerCallbacks {
        @Override
        public void onBannerLoaded(int i, boolean b) {
            Ads.updateBanner(adView);
        }

        @Override
        public void onBannerFailedToLoad() {
            //EventCollector.logException("appodeal_no_banner");
            AdsUtilsCommon.bannerFailed(AppodealBannerProvider.this);
        }

        @Override
        public void onBannerShown() {

        }

        @Override
        public void onBannerShowFailed() {
            //EventCollector.logException("appodeal_no_banner");
            AdsUtilsCommon.bannerFailed(AppodealBannerProvider.this);
        }

        @Override
        public void onBannerClicked() {

        }

        @Override
        public void onBannerExpired() {

        }
    }
}
