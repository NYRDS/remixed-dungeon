package com.nyrds.pixeldungeon.support;

import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;

import java.util.Map;

import androidx.annotation.Nullable;

class AdsUtilsCommon {

    static public void bannerFailed(IBannerProvider provider) {
        incFailCount(AdsUtils.bannerFails,provider);
        //tryNextBanner();
    }

    static public void interstitialFailed(IInterstitialProvider provider, InterstitialPoint retTo) {
        incFailCount(AdsUtils.interstitialFails,provider);
        retTo.returnToWork(false);
        //tryNextInterstitial(retTo);
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
    private static <T> T choseLessFailedFrom(Map<T,Integer> map, int maxFails) {
        int minima = Integer.MAX_VALUE;

        T chosenProvider = null;

        for (T provider: map.keySet()) {
            Integer failRate = map.get(provider);
            if(failRate!=null && failRate<minima) {
                minima = failRate;
                chosenProvider = provider;
            }
        }

        if(minima<maxFails) {
            return chosenProvider;
        }
        return null;
    }

    private static void tryNextInterstitial(final InterstitialPoint retTo) {
        final IInterstitialProvider chosenProvider = choseLessFailedFrom(AdsUtils.interstitialFails, Integer.MAX_VALUE);

        if(chosenProvider!=null) {
            Game.instance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chosenProvider.showInterstitial(retTo);
                }
            });
        } else {
            retTo.returnToWork(false);
        }
    }

    static private void tryNextBanner() {
        final IBannerProvider chosenProvider = choseLessFailedFrom(AdsUtils.bannerFails, Integer.MAX_VALUE);

        if(chosenProvider!=null) {
            Game.instance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chosenProvider.displayBanner();
                }
            });
        }
    }

    static void displayTopBanner() {
        if(AdsUtils.bannerIndex()<0) {
            tryNextBanner();
        }
    }

    static void showInterstitial(InterstitialPoint retTo) {
        tryNextInterstitial(retTo);
    }

    interface IBannerProvider {
        void displayBanner();
    }

    interface IInterstitialProvider {
        void showInterstitial(final InterstitialPoint ret);
    }
}
