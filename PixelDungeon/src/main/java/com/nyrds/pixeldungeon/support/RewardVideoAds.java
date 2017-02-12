package com.nyrds.pixeldungeon.support;

import android.widget.Toast;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.RewardedVideoCallbacks;
import com.appodeal.ads.utils.Log;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.PixelDungeon;

/**
 * Created by mike on 30.01.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class RewardVideoAds {

	private static RewardedVideoAd mCinemaRewardAd;
	//private static RewardVideoAdListener rewardVideoAdListener;

	private static InterstitialPoint returnTo;

	public static void initCinemaRewardVideo() {
		//if (Util.isConnectedToInternet())
			Game.instance().runOnUiThread(new Runnable() {
				@Override
				public void run() {

					String appKey = "843ce15d3d6555bd92b2eb12f63bd87b363f9482ef7174b3";
					Appodeal.disableLocationPermissionCheck();
					//Appodeal.disableNetwork(PixelDungeon.instance(),"adcolony");
					//Appodeal.disableNetwork(PixelDungeon.instance(),"applovin");
					Appodeal.setLogLevel(Log.LogLevel.verbose);
					Appodeal.setTesting(true);

					Appodeal.initialize(PixelDungeon.instance(), appKey, Appodeal.REWARDED_VIDEO);
					Appodeal.setRewardedVideoCallbacks(new RewardedVideoCallbacks() {
						private Toast mToast;
						@Override
						public void onRewardedVideoLoaded() {
							//showToast("onRewardedVideoLoaded");
						}
						@Override
						public void onRewardedVideoFailedToLoad() {
							//showToast("onRewardedVideoFailedToLoad");
						}
						@Override
						public void onRewardedVideoShown() {
							//showToast("onRewardedVideoShown");
						}
						@Override
						public void onRewardedVideoFinished(int amount, String name) {
							//showToast(String.format("onRewardedVideoFinished. Reward: %d %s", amount, name));

						}
						@Override
						public void onRewardedVideoClosed(final boolean finished) {
							//showToast(String.format("onRewardedVideoClosed,  finished: %s", finished));
							returnTo.returnToWork(finished);

						}
					});


					/*mCinemaRewardAd = MobileAds.getRewardedVideoAdInstance(Game.instance());
					mCinemaRewardAd.setRewardedVideoAdListener(rewardVideoAdListener);
					mCinemaRewardAd.loadAd(Game.getVar(R.string.cinemaRewardAdUnitId), new AdRequest.Builder().build());
					*/
				}
			});
	}

	public static void showCinemaRewardVideo(InterstitialPoint ret) {
		returnTo = ret;
		Game.instance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) {
					Appodeal.show(PixelDungeon.instance(), Appodeal.REWARDED_VIDEO);
				} else {
					returnTo.returnToWork(false);
				}
				/*if (mCinemaRewardAd.isLoaded()) {
					mCinemaRewardAd.show();
				}*/
			}
		});
	}





	/*private class RewardVideoAdListener implements RewardedVideoAdListener {

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
	};*/



}
