package com.nyrds.retrodungeon.support;

import com.watabou.noosa.InterstitialPoint;

/**
 * Created by mike on 03.04.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class RewardVideo {
	static public void init() {
		if(!isReady()) {
			AppodealRewardVideo.initCinemaRewardVideo();
			GoogleRewardVideoAds.initCinemaRewardVideo();
		}
	}

	static public boolean isReady() {
		return AppodealRewardVideo.isReady() || GoogleRewardVideoAds.isReady();
	}

	public static void showCinemaRewardVideo(InterstitialPoint ret) {
		if (AppodealRewardVideo.isReady()) {
			AppodealRewardVideo.showCinemaRewardVideo(ret);
			return;
		}

		if(GoogleRewardVideoAds.isReady()) {
			GoogleRewardVideoAds.showCinemaRewardVideo(ret);
			return;
		}

		ret.returnToWork(false);
	}
}
