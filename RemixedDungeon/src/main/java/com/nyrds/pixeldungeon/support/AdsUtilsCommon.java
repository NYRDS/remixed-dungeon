package com.nyrds.pixeldungeon.support;

import androidx.annotation.MainThread;

import com.nyrds.platform.game.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

class AdsUtilsCommon {

    static int bannerAttempts;
    static int interstitialAttempts;
    static int rvAttempts;

    static void bannerFailed(IBannerProvider provider) {
        incFailCount(AdsUtils.bannerFails,provider);
        //if(bannerAttempts-- > 0) {
        //    tryNextBanner();
        //}
    }

    static void interstitialFailed(IInterstitialProvider provider, InterstitialPoint retTo) {
        incFailCount(AdsUtils.interstitialFails,provider);
        if(interstitialAttempts-- > 0) {
            tryNextInterstitial(retTo);
        } else {
            retTo.returnToWork(false);
        }
    }

    static void rewardVideoFailed(IRewardVideoProvider provider) {
        if(rvAttempts-- > 0) {
            incFailCount(AdsUtils.rewardVideoFails, provider);
        }
        GLog.debug("reward video load failed: %s", provider.getClass().getSimpleName());
    }

    private static <T> void incFailCount(Map<T,Integer> map, T provider) {
        Integer failCount = map.get(provider);
        if(failCount!=null) {
            map.put(provider,failCount+1);
        } else {
            map.put(provider,1);
        }
    }

    @Nullable
    private static <T extends IProvider> T choseLessFailedFrom(Map<T,Integer> map, int maxFails) {
        int minima = Integer.MAX_VALUE;

        T chosenProvider = null;

        for (T provider: map.keySet()) {
            if(provider.isReady()) {
                Integer failRate = map.get(provider);
                if (failRate != null && failRate < minima) {
                    minima = failRate;
                    chosenProvider = provider;
                }
            }
        }

        if(minima<maxFails) {
            return chosenProvider;
        }
        return null;
    }

    private static void tryNextRewardVideo(final InterstitialPoint retTo) {
        final IRewardVideoProvider chosenProvider = choseLessFailedFrom(AdsUtils.rewardVideoFails, Integer.MAX_VALUE);

        if(chosenProvider!=null) {
            Game.runOnMainThread(() -> chosenProvider.showRewardVideo(retTo));
        } else {
            retTo.returnToWork(false);
        }
    }

    private static void tryNextInterstitial(final InterstitialPoint retTo) {
        final IInterstitialProvider chosenProvider = choseLessFailedFrom(AdsUtils.interstitialFails, Integer.MAX_VALUE);

        if(chosenProvider!=null) {
            Game.runOnMainThread(() -> chosenProvider.showInterstitial(retTo));
        } else {
            retTo.returnToWork(false);
        }
    }

    static private void tryNextBanner() {
        IBannerProvider chosenProvider = choseLessFailedFrom(AdsUtils.bannerFails, Integer.MAX_VALUE);

        if(chosenProvider!=null) {
            Game.runOnMainThread(chosenProvider::displayBanner);
        }
    }

    static void displayTopBanner() {
        if(AdsUtils.bannerIndex() <0) {
            tryNextBanner();
        }
    }

    static void showInterstitial(InterstitialPoint retTo) {
        interstitialAttempts = 3;
        tryNextInterstitial(retTo);
    }


    static void showRewardVideo(InterstitialPoint retTo) {
        tryNextRewardVideo(retTo);
    }

    @MainThread
    static boolean isRewardVideoReady() {
        for(IRewardVideoProvider provider: AdsUtils.rewardVideoFails.keySet()) {
            if(provider.isReady()) {
                return true;
            }
        }
        return false;
    }

    public static void rewardVideoLoaded(IRewardVideoProvider provider) {
        GLog.debug("reward video loaded: %s", provider.getClass().getSimpleName());

    }

    interface IProvider {
        boolean isReady();
    }

    interface IBannerProvider extends IProvider {
        void displayBanner();
    }

    interface IInterstitialProvider extends IProvider  {
        void showInterstitial(final InterstitialPoint ret);
    }

    interface IRewardVideoProvider extends IProvider {
        void showRewardVideo(final InterstitialPoint ret);
    }
}
