package com.nyrds.pixeldungeon.support;

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

		if(!AppodealAdapter.isVideoInitialized()) {
			AppodealAdapter.initRewardedVideo();
		}
	}

	@UiThread
	static public boolean isReady() {
		return AppodealAdapter.isVideoReady() || GoogleRewardVideoAds.isVideoReady();
	}

	@UiThread
	public static void showCinemaRewardVideo(InterstitialPoint ret) {
		if(GoogleRewardVideoAds.isVideoReady()) {
			GoogleRewardVideoAds.showCinemaRewardVideo(ret);
			return;
		}

		if (AppodealAdapter.isVideoReady()) {
			AppodealAdapter.showCinemaRewardVideo(ret);
			return;
		}

		ret.returnToWork(false);
	}
}
