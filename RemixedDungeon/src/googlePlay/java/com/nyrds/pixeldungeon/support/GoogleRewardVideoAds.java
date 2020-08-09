package com.nyrds.pixeldungeon.support;

import androidx.annotation.MainThread;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;

/**
 * Created by mike on 30.01.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class GoogleRewardVideoAds implements AdsUtilsCommon.IRewardVideoProvider {

	private static RewardedVideoAd mCinemaRewardAd;
	private static InterstitialPoint returnTo;


	public GoogleRewardVideoAds() {
		Game.instance().runOnUiThread(this::loadNextVideo);
	}


	@MainThread
	private void loadNextVideo() {
		mCinemaRewardAd = MobileAds.getRewardedVideoAdInstance(Game.instance());
		mCinemaRewardAd.setRewardedVideoAdListener(new RewardVideoAdListener());
		mCinemaRewardAd.loadAd(Game.getVar(R.string.cinemaRewardAdUnitId), AdMob.makeAdRequest());
	}

	@MainThread
	public  boolean isReady() {
		return mCinemaRewardAd != null && mCinemaRewardAd.isLoaded();
	}

	@Override
	public void showRewardVideo(InterstitialPoint ret) {
		returnTo = ret;
		mCinemaRewardAd.show();
	}

	private class RewardVideoAdListener implements RewardedVideoAdListener {

		private boolean rewarded = false;

		@Override
		public void onRewardedVideoAdLoaded() {
			//GLog.i("onRewardedVideoAdLoaded");
		}

		@Override
		public void onRewardedVideoAdOpened() {
			rewarded = false;
			//GLog.i("onRewardedVideoAdOpened");
		}

		@Override
		public void onRewardedVideoStarted() {
			rewarded = false;
			//GLog.i("onRewardedVideoStarted");
		}

		@Override
		public void onRewardedVideoAdClosed() {
			Game.instance().runOnUiThread(GoogleRewardVideoAds.this::loadNextVideo);
			returnTo.returnToWork(rewarded);
		}

		@Override
		public void onRewarded(RewardItem rewardItem) {
			//GLog.i("onRewarded %s", rewardItem);
			returnTo.returnToWork(true);
		}

		@Override
		public void onRewardedVideoAdLeftApplication() {
			//GLog.i("onRewardedVideoAdLeftApplication");
			returnTo.returnToWork(true);
		}

		@Override
		public void onRewardedVideoAdFailedToLoad(int i) {
			//GLog.i("onRewardedVideoFailedToLoad %d", i);
		}

		@Override
		public void onRewardedVideoCompleted()
		{
			rewarded = true;
			//GLog.i("onRewardedVideoCompleted");
		}
	}

}
