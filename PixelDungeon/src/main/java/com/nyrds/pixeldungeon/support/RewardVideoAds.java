package com.nyrds.pixeldungeon.support;

import android.util.Log;
import android.widget.Toast;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.RewardedVideoCallbacks;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.PixelDungeon;

/**
 * Created by mike on 30.01.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class RewardVideoAds {

	private static RewardedVideoAd mCinemaRewardAd;
	//private static RewardVideoAdListener rewardVideoAdListener;

	public static void initCinemaRewardVideo() {
		if (Ads.googleAdsUsable() && Util.isConnectedToInternet())
			Game.instance().runOnUiThread(new Runnable() {
				@Override
				public void run() {

					String appKey = "843ce15d3d6555bd92b2eb12f63bd87b363f9482ef7174b3";
					Appodeal.initialize(PixelDungeon.instance(), appKey, Appodeal.REWARDED_VIDEO);

					Appodeal.setRewardedVideoCallbacks(new RewardedVideoCallbacks() {
						private Toast mToast;
						@Override
						public void onRewardedVideoLoaded() {
							showToast("onRewardedVideoLoaded");
						}
						@Override
						public void onRewardedVideoFailedToLoad() {
							showToast("onRewardedVideoFailedToLoad");
						}
						@Override
						public void onRewardedVideoShown() {
							showToast("onRewardedVideoShown");
						}
						@Override
						public void onRewardedVideoFinished(int amount, String name) {
							showToast(String.format("onRewardedVideoFinished. Reward: %d %s", amount, name));
						}
						@Override
						public void onRewardedVideoClosed(boolean finished) {
							showToast(String.format("onRewardedVideoClosed,  finished: %s", finished));
						}
						void showToast(final String text) {
							if (mToast == null) {
								mToast = Toast.makeText(PixelDungeon.instance(), text, Toast.LENGTH_SHORT);
							}
							mToast.setText(text);
							mToast.setDuration(Toast.LENGTH_SHORT);
							mToast.show();
						}
					});

					/*mCinemaRewardAd = MobileAds.getRewardedVideoAdInstance(Game.instance());
					mCinemaRewardAd.setRewardedVideoAdListener(rewardVideoAdListener);
					mCinemaRewardAd.loadAd(Game.getVar(R.string.cinemaRewardAdUnitId), new AdRequest.Builder().build());
					*/
				}
			});
	}

	public static void showCinemaRewardVideo() {
		Game.instance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Appodeal.show(PixelDungeon.instance(), Appodeal.REWARDED_VIDEO);
				Appodeal.isLoaded(Appodeal.REWARDED_VIDEO);
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
