package com.nyrds.pixeldungeon.support;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.AdRequestConfiguration;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.rewarded.Reward;
import com.yandex.mobile.ads.rewarded.RewardedAd;
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoader;

import org.jetbrains.annotations.Nullable;

/**
 * Created by mike on 30.01.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class YandexRewardVideoAds implements AdsUtilsCommon.IRewardVideoProvider {

	@Nullable
	private RewardedAdLoader mRewardedAdLoader = null;
	private RewardedAd mCinemaRewardAd;
	private InterstitialPoint returnTo = new Utils.SpuriousReturn();

	private boolean rewardEarned = false;
	private final String adId;


	public YandexRewardVideoAds(String id) {
		adId = id;
		GameLoop.runOnMainThread(this::loadNextVideo);
	}

	@MainThread
	private void loadNextVideo() {
		try {
			EventCollector.logEvent("yandex_reward_requested");
			mRewardedAdLoader = new RewardedAdLoader(Game.instance());
			mRewardedAdLoader.setAdLoadListener(new RewardedAdLoadListener() {
				@Override
				public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
					AdsUtilsCommon.rewardVideoFailed(YandexRewardVideoAds.this);
				}

				@Override
				public void onAdLoaded(@NonNull final RewardedAd rewardedAd) {
					mCinemaRewardAd = rewardedAd;
					AdsUtilsCommon.rewardVideoLoaded(YandexRewardVideoAds.this);
				}

			});
			loadRewardedAd();

		} catch (Exception e) {
			AdsUtilsCommon.rewardVideoFailed(YandexRewardVideoAds.this);
			EventCollector.logException(e, "YandexRewardVideoAds");
		}
	}

	private void loadRewardedAd() {
		if (mRewardedAdLoader != null ) {
			final AdRequestConfiguration adRequestConfiguration =
					new AdRequestConfiguration.Builder(adId).build();
			mRewardedAdLoader.loadAd(adRequestConfiguration);
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
			rewardEarned = false;
			mCinemaRewardAd.setAdEventListener(new RewardedAdEventListener() {
				@Override
				public void onAdShown() {
					// Called when an ad is shown.
					EventCollector.logEvent("yandex_reward_shown");
				}

				@Override
				public void onAdFailedToShow(@NonNull final AdError adError) {
					EventCollector.logEvent("yandex_reward_show_failed", adError.toString());

					returnTo.returnToWork(rewardEarned);
					// Called when an InterstitialAd failed to show.
				}

				@Override
				public void onAdDismissed() {
					GLog.debug("onAdDismissed");
					// Called when an ad is dismissed.
					// Clean resources after Ad dismissed

					if (mCinemaRewardAd != null) {
						mCinemaRewardAd.setAdEventListener(null);
						mCinemaRewardAd = null;
					}

					// Now you can preload the next reward ad.
					loadRewardedAd();
					returnTo.returnToWork(rewardEarned);
				}

				@Override
				public void onAdClicked() {
					GLog.debug("onAdClicked");
					// Called when a click is recorded for an ad.
				}

				@Override
				public void onAdImpression(@Nullable final ImpressionData impressionData) {
					// Called when an impression is recorded for an ad.
					EventCollector.logEvent("yandex_reward_impression", impressionData.toString());
					GLog.debug("onAdImpression: %s", impressionData);
				}

				@Override
				public void onRewarded(@NonNull final Reward reward) {
					rewardEarned = true;
					EventCollector.logEvent("yandex_reward_rewarded");
				}
			});
			mCinemaRewardAd.show(Game.instance());
		}
	}
}
