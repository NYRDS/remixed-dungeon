package com.nyrds.pixeldungeon.support;

import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;

/**
 * Created by mike on 30.01.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class RewardVideoAds {

	private static RewardedVideoAd mCinemaRewardAd;
	private static RewardVideoAdListener rewardVideoAdListener;

	public static void initCinemaRewardVideo() {
		if (Ads.googleAdsUsable() && Util.isConnectedToInternet())
			Game.instance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mCinemaRewardAd = MobileAds.getRewardedVideoAdInstance(Game.instance());
					mCinemaRewardAd.setRewardedVideoAdListener(rewardVideoAdListener);
					mCinemaRewardAd.loadAd(Game.getVar(R.string.cinemaRewardAdUnitId), new AdRequest.Builder().build());
				}
			});
	}

	public static void showCinemaRewardVideo() {
		Game.instance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mCinemaRewardAd.isLoaded()) {
					mCinemaRewardAd.show();
				}
			}
		});
	}

	private class RewardVideoAdListener implements RewardedVideoAdListener {

		@Override
		public void onRewardedVideoAdLoaded() {
			Log.i("reward video","onRewardedVideoAdLoaded()");
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
		}

		@Override
		public void onRewarded(RewardItem rewardItem) {
			Log.i("reward video","onRewarded(RewardItem rewardItem)");
		}

		@Override
		public void onRewardedVideoAdLeftApplication() {
			Log.i("reward video","onRewardedVideoAdLeftApplication");
		}

		@Override
		public void onRewardedVideoAdFailedToLoad(int i) {
			Log.i("reward video","onRewardedVideoAdFailedToLoad(int i)");
		}
	};

}
