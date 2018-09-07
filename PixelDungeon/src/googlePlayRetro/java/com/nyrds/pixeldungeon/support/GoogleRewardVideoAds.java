package com.nyrds.pixeldungeon.support;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;

/**
 * Created by mike on 30.01.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class GoogleRewardVideoAds {

	private static RewardedVideoAd mCinemaRewardAd;
	private static RewardVideoAdListener rewardVideoAdListener = new RewardVideoAdListener();
	private static InterstitialPoint returnTo;
	private static volatile boolean loaded = false;


	public static void initCinemaRewardVideo() {
		if (Ads.googleAdsUsable())
			Game.instance().runOnUiThread(new Runnable() {
				@Override
				public void run() {

					mCinemaRewardAd = MobileAds.getRewardedVideoAdInstance(Game.instance());
					mCinemaRewardAd.setRewardedVideoAdListener(rewardVideoAdListener);

					EventCollector.startTiming("google reward video");
					mCinemaRewardAd.loadAd(Game.getVar(R.string.cinemaRewardAdUnitId), Ads.makeAdRequest());
				}
			});
	}

	public static boolean isReady() {
		return loaded;
	}


	public static void showCinemaRewardVideo(InterstitialPoint ret) {
		returnTo = ret;
		Game.instance().runOnUiThread (new Runnable() {
			@Override
			public void run() {
				if (mCinemaRewardAd.isLoaded()) {
					mCinemaRewardAd.show();
				}else {
					returnTo.returnToWork(false);
				}
			}
		});
	}

	private static class RewardVideoAdListener implements RewardedVideoAdListener {

		@Override
		public void onRewardedVideoAdLoaded() {
			EventCollector.stopTiming("google reward video","google reward video","ok","");
			loaded = true;
		}

		@Override
		public void onRewardedVideoAdOpened() { }

		@Override
		public void onRewardedVideoStarted() { }

		@Override
		public void onRewardedVideoAdClosed() {
			Game.instance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					loaded = false;
					mCinemaRewardAd.loadAd(Game.getVar(R.string.cinemaRewardAdUnitId), Ads.makeAdRequest());
				}
			});
		}

		@Override
		public void onRewarded(RewardItem rewardItem) {
			returnTo.returnToWork(true);
		}

		@Override
		public void onRewardedVideoAdLeftApplication() {
		}

		@Override
		public void onRewardedVideoAdFailedToLoad(int i) {
			EventCollector.stopTiming("google reward video","google reward video","fail","");
		}
	}

}
