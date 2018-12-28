package com.nyrds.pixeldungeon.support;

import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;

import androidx.annotation.UiThread;

/**
 * Created by mike on 03.04.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class RewardVideo {
	static public void init() {
		if(!GoogleRewardVideoAds.isVideoInitialized()){
			GoogleRewardVideoAds.initCinemaRewardVideo();
		}
	}

	@UiThread
	static public boolean isReady() {
		return GoogleRewardVideoAds.isVideoReady();
	}

	@UiThread
	public static void showCinemaRewardVideo(InterstitialPoint ret) {
        Game.instance().runOnUiThread ( () -> {
            if (GoogleRewardVideoAds.isVideoReady()) {
                GoogleRewardVideoAds.showCinemaRewardVideo(ret);
                return;
            }
            Game.pushUiTask( () -> {
                ret.returnToWork(false);
            });
		});
	}
}
