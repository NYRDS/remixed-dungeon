package com.nyrds.pixeldungeon.support;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

/**
 * Created by mike on 30.01.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class GoogleRewardVideoAds implements AdsUtilsCommon.IRewardVideoProvider {

	private RewardedAd mCinemaRewardAd;
	private InterstitialPoint returnTo = new Utils.SpuriousReturn();

	private boolean rewardEarned = false;
	private final String adId;


	public GoogleRewardVideoAds(String id) {
		adId = id;
		GameLoop.runOnMainThread(this::loadNextVideo);
	}

	@MainThread
	private void loadNextVideo() {
		EventCollector.logEvent("admob_reward_requested");
		try {
			FullScreenContentCallback fullScreenContentCallback =
					new FullScreenContentCallback() {
						@Override
						public void onAdShowedFullScreenContent() {
							rewardEarned = false;
						}

						@Override
						public void onAdDismissedFullScreenContent() {
							mCinemaRewardAd = null;
							EventCollector.logEvent("admob_reward_shown");
							GameLoop.runOnMainThread(GoogleRewardVideoAds.this::loadNextVideo);
							returnTo.returnToWork(rewardEarned);
							rewardEarned = false;
						}
					};

			RewardedAd.load(Game.instance(),
					adId,
					AdMob.makeAdRequest(),
					new RewardedAdLoadCallback() {
						@Override
						public void onAdLoaded(@NotNull RewardedAd ad) {
							mCinemaRewardAd = ad;
							mCinemaRewardAd.setFullScreenContentCallback(fullScreenContentCallback);
							AdsUtilsCommon.rewardVideoLoaded(GoogleRewardVideoAds.this);
							EventCollector.logEvent("admob_reward_loaded");
						}

						@Override
						public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
							AdsUtilsCommon.rewardVideoFailed(GoogleRewardVideoAds.this);
							EventCollector.logEvent("admob_reward_failed", loadAdError.toString());
						}
					});
		} catch (Exception e) {
			AdsUtilsCommon.rewardVideoFailed(GoogleRewardVideoAds.this);
			EventCollector.logException(e, "GoogleRewardVideoAds");
		}
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
						EventCollector.logEvent("admob_reward_earned", rewardItem.toString());
						returnTo.returnToWork(true);
					}

			);
		}
	}
}
