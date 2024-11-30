package com.nyrds.pixeldungeon.support;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.RewardedVideoCallbacks;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.RemixedDungeon;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.utils.Utils;

class AppodealRewardVideoProvider implements AdsUtilsCommon.IRewardVideoProvider {

    private static InterstitialPoint returnTo = new Utils.SpuriousReturn();

    public AppodealRewardVideoProvider() {

        GameLoop.runOnMainThread(() -> {
            AppodealAdapter.init();

            Appodeal.cache(RemixedDungeon.instance(), Appodeal.REWARDED_VIDEO);
            Appodeal.setAutoCache(Appodeal.REWARDED_VIDEO, true);
            EventCollector.logEvent("appodeal_reward_requested");
            Appodeal.setRewardedVideoCallbacks(new RewardedVideoCallbacks() {

                @Override
                public void onRewardedVideoLoaded(boolean b) {
                    EventCollector.logEvent("appodeal_reward_loaded");
                    AdsUtilsCommon.rewardVideoLoaded(AppodealRewardVideoProvider.this);
                }

                @Override
                public void onRewardedVideoFailedToLoad() {
                    EventCollector.logEvent("appodeal_failed_To_load_reward");
                    AdsUtilsCommon.rewardVideoFailed(AppodealRewardVideoProvider.this);
                }

                @Override
                public void onRewardedVideoShown() {
                }

                @Override
                public void onRewardedVideoShowFailed() {
                    EventCollector.logEvent("appodeal_reward_failed");
                    returnTo.returnToWork(false);
                }

                @Override
                public void onRewardedVideoFinished(double amount, String name) {
                    EventCollector.logEvent("appodeal_reward_shown");
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
