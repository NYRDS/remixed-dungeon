package com.nyrds.pixeldungeon.support;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.nyrds.platform.game.Game;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

/**
 * Created by mike on 30.01.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class GoogleRewardVideoAds implements AdsUtilsCommon.IRewardVideoProvider {

	private static RewardedAd mCinemaRewardAd;
	private static InterstitialPoint returnTo;

	private static boolean rewardEarned = false;


	public GoogleRewardVideoAds() {
		Game.instance().runOnUiThread(this::loadNextVideo);
	}


	@MainThread
	private void loadNextVideo() {

		FullScreenContentCallback fullScreenContentCallback =
				new FullScreenContentCallback() {
					@Override
					public void onAdShowedFullScreenContent() {
						rewardEarned = false;
					}

					@Override
					public void onAdDismissedFullScreenContent() {
						mCinemaRewardAd = null;
						GLog.debug("reward state "+ rewardEarned);
						Game.instance().runOnUiThread(GoogleRewardVideoAds.this::loadNextVideo);
						returnTo.returnToWork(rewardEarned);
						rewardEarned = false;
					}
				};

        RewardedAd.load(Game.instance(),
				ModdingMode.getRewardedVideoId(),
				AdMob.makeAdRequest(),
				new RewardedAdLoadCallback() {
					@Override
					public void onAdLoaded(@NotNull RewardedAd ad) {
						mCinemaRewardAd = ad;
						mCinemaRewardAd.setFullScreenContentCallback(fullScreenContentCallback);
					}

					@Override
					public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
						AdsUtilsCommon.rewardVideoFailed(GoogleRewardVideoAds.this);
					}
				});
	}

	@MainThread
	public  boolean isReady() {
		return mCinemaRewardAd != null;
	}

	@Override
	public void showRewardVideo(InterstitialPoint ret) {
		returnTo = ret;
		if(mCinemaRewardAd!=null) {
			mCinemaRewardAd.show(Game.instance(),
					rewardItem -> {
						rewardEarned = true;
						GLog.debug("reward: %s %d", rewardItem.getType(), rewardItem.getAmount());
						returnTo.returnToWork(true);
					}

			);
		}
	}
}
