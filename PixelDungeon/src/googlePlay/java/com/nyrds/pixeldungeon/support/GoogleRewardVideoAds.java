package com.nyrds.pixeldungeon.support;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;

import androidx.annotation.MainThread;

/**
 * Created by mike on 30.01.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class GoogleRewardVideoAds {

	public static final String GOOGLE_REWARD_VIDEO = "google reward video";
	private static RewardedVideoAd mCinemaRewardAd;
	private static InterstitialPoint returnTo;


	public static void initCinemaRewardVideo() {

		if(isVideoInitialized()) {
			return;
		}

		Game.instance().runOnUiThread(() -> loadNextVideo());
	}

	@MainThread
	private static void loadNextVideo() {
		EventCollector.startTrace(GOOGLE_REWARD_VIDEO);

		mCinemaRewardAd = MobileAds.getRewardedVideoAdInstance(Game.instance());
		mCinemaRewardAd.setRewardedVideoAdListener(new RewardVideoAdListener());
		mCinemaRewardAd.loadAd(Game.getVar(R.string.cinemaRewardAdUnitId), AdMob.makeAdRequest());
	}

	@MainThread
	public static boolean isVideoReady() {
		return mCinemaRewardAd != null && mCinemaRewardAd.isLoaded();
	}

	@MainThread
	public static void showCinemaRewardVideo(InterstitialPoint ret) {
		returnTo = ret;

		Game.instance().runOnUiThread (() -> {
			if (mCinemaRewardAd.isLoaded()) {
				mCinemaRewardAd.show();
			}else {
				returnTo.returnToWork(false);
			}
		});
	}

	public static boolean isVideoInitialized() {
		return mCinemaRewardAd != null;
	}

	private static class RewardVideoAdListener implements RewardedVideoAdListener {

		private boolean videoCompleted = false;

		@Override
		public void onRewardedVideoAdLoaded() {
			EventCollector.stopTrace(GOOGLE_REWARD_VIDEO,"google reward video","ok","");
			videoCompleted = false;
		}

		@Override
		public void onRewardedVideoAdOpened() { }

		@Override
		public void onRewardedVideoStarted() { }

		@Override
		public void onRewardedVideoAdClosed() {
			Game.instance().runOnUiThread(() -> loadNextVideo());
			returnTo.returnToWork(videoCompleted);
		}

		@Override
		public void onRewarded(RewardItem rewardItem) {
			videoCompleted = true;
		}

		@Override
		public void onRewardedVideoAdLeftApplication() {
		}

		@Override
		public void onRewardedVideoAdFailedToLoad(int i) {
			EventCollector.stopTrace(GOOGLE_REWARD_VIDEO,"google reward video","fail","");
		}

		@Override
		public void onRewardedVideoCompleted() {
			videoCompleted = true;
		}
	}

}
