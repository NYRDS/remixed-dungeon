package com.nyrds.pixeldungeon.support;

import com.watabou.noosa.Game;

class AdsUtilsCommon {

    static public void bannerFailed(IBannerProvider provider) {
        if(AdsUtils.fails.containsKey(provider)) {
            AdsUtils.fails.put(provider, AdsUtils.fails.get(provider)+1);
        }   else {
            AdsUtils.fails.put(provider,1);
        }
        tryNextBanner();
    }

    static private void tryNextBanner() {
        int minima = 3;

        IBannerProvider chosenProvider = null;

        for (IBannerProvider provider: AdsUtils.fails.keySet()) {
            if(AdsUtils.fails.get(provider)<minima) {
                minima = AdsUtils.fails.get(provider);
                chosenProvider = provider;
            }
        }

        if(minima < 3 && chosenProvider!=null) {
            final IBannerProvider finalChosenProvider = chosenProvider;
            Game.instance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finalChosenProvider.displayBanner();
                }
            });
        }
    }

    static void displayTopBanner() {
        tryNextBanner();
    }

    interface IBannerProvider {
        void displayBanner();
    }
}
