package com.nyrds.pixeldungeon.support;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.RewardedVideoCallbacks;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.RemixedDungeon;

class AppodealRewardVideoProvider implements AdsUtilsCommon.IRewardVideoProvider {

    private static InterstitialPoint returnTo;

    public static void init() {

        Game.instance().runOnUiThread(() -> {
            AppodealAdapter.init();

            Appodeal.cache(RemixedDungeon.instance(), Appodeal.REWARDED_VIDEO);
            Appodeal.setAutoCache(Appodeal.REWARDED_VIDEO, true);

            Appodeal.setRewardedVideoCallbacks(new RewardedVideoCallbacks() {

                @Override
                public void onRewardedVideoLoaded(boolean b) {
                }

                @Override
                public void onRewardedVideoFailedToLoad() {
                }

                @Override
                public void onRewardedVideoShown() {
                }

                @Override
                public void onRewardedVideoShowFailed() {
                    returnTo.returnToWork(false);
                }

                @Override
                public void onRewardedVideoFinished(double amount, String name) {
                    returnTo.returnToWork(true);
                }

                @Override
                public void onRewardedVideoClosed(final boolean finished) {
                    returnTo.returnToWork(finished);
                }

                @Override
                public void onRewardedVideoExpired() {
                }

                @Override
                public void onRewardedVideoClicked() {
                    returnTo.returnToWork(true);
                }
            });
        });
    }


    @Override
    public boolean isReady() {
        return Appodeal.isLoaded(Appodeal.REWARDED_VIDEO);
    }

    @Override
    public void showRewardVideo(InterstitialPoint ret) {
        returnTo = ret;
        Appodeal.show(RemixedDungeon.instance(), Appodeal.REWARDED_VIDEO);
    }
}
