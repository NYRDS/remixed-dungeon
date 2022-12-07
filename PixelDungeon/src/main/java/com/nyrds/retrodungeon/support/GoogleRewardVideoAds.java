package com.nyrds.retrodungeon.support;

import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.nyrds.retrodungeon.ml.R;
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
					mCinemaRewardAd.loadAd(Game.getVar(R.string.cinemaRewardAdUnitId), new AdRequest.Builder().build());
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
			Log.i("reward video","onRewardedVideoAdLoaded()");
			loaded = true;
		}

		@Override
		public void onRewardedVideoAdOpened() {
			Log.i("reward video","onRewardedVideoAdOpened()");
		}

		@Override
		public void onRewardedVideoStarted() {
			Log.i("reward video","onRewardedVideoStarted()");
		}

		@Override
		public void onRewardedVideoAdClosed() {
			Log.i("reward video","onRewardedVideoAdClosed()");
			Game.instance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					loaded = false;
					mCinemaRewardAd.loadAd(Game.getVar(R.string.cinemaRewardAdUnitId), new AdRequest.Builder().build());
				}
			});
		}

		@Override
		public void onRewarded(RewardItem rewardItem) {
			Log.i("reward video","onRewarded(RewardItem rewardItem)");
			returnTo.returnToWork(true);
		}

		@Override
		public void onRewardedVideoAdLeftApplication() {
			Log.i("reward video","onRewardedVideoAdLeftApplication");
		}

		@Override
		public void onRewardedVideoAdFailedToLoad(int i) {
			Log.i("reward video","onRewardedVideoAdFailedToLoad(int i)");
		}
	}

}
