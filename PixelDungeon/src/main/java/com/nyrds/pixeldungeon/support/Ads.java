package com.nyrds.pixeldungeon.support;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerView;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.nyrds.android.util.Flavours;
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.PixelDungeon;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 24.05.2016.
 */
public class Ads {
	private static InterstitialAd  mSaveAndLoadAd;
	private static InterstitialAd  mEasyModeSmallScreenAd;

	private static boolean isSmallScreen() {
		return (Game.width() < 400 || Game.height() < 400);
	}

	private static boolean needDisplaySmallScreenEasyModeIs() {
		return Game.getDifficulty() == 0 && isSmallScreen() && PixelDungeon.donated() == 0;
	}

	public static boolean googleAdsUsable() {
		return Flavours.haveAds();
	}


	public static AdRequest makeAdRequest() {
		if(EuConsent.getConsentLevel() < EuConsent.PERSONALIZED) {
			Bundle extras = new Bundle();
			extras.putString("npa", "1");

			return new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();
		}

		return new AdRequest.Builder().build();
	}

	private static void displayOwnEasyModeBanner() {
		if (isSmallScreen()) {
		} else {
			OwnAds.displayBanner();
		}
	}

	private static int bannerIndex() {
		int childs = Game.instance().getLayout().getChildCount();
		for (int i = 0; i< childs;++i)
		{
			View view = Game.instance().getLayout().getChildAt(i);
			if(view instanceof AdView || view instanceof WebView || view instanceof BannerView) {
				return i;
			}
		}
		return -1;
	}

	private static void displayGoogleEasyModeBanner() {
		if (isSmallScreen()) {
			initEasyModeIntersitial();
		} else {
			Game.instance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (bannerIndex()<0) {

						AdView adView = new AdView(Game.instance());
						adView.setAdSize(AdSize.SMART_BANNER);
						adView.setAdUnitId(Game.getVar(R.string.easyModeAdUnitId));
						adView.setBackgroundColor(Color.TRANSPARENT);
						adView.setAdListener(new AdListener(){
/*
							@Override
							public void onAdLoaded() {
								onAdFailedToLoad(0);
							}
*/
							public void onAdFailedToLoad(int var1) {

								Game.instance().runOnUiThread(new Runnable() {
									@Override
									public void run() {

										removeEasyModeBanner();

										AppodealRewardVideo.init();
										Appodeal.cache(Game.instance(), Appodeal.BANNER);
										BannerView adView = Appodeal.getBannerView(Game.instance());
										Game.instance().getLayout().addView(adView, 0);

										Appodeal.show(Game.instance(), Appodeal.BANNER_VIEW);
									}});
							}
						});

						Game.instance().getLayout().addView(adView, 0);
						adView.loadAd(makeAdRequest());




						Game.setNeedSceneRestart(true);
					}
				}
			});
		}
	}

	public static void displayEasyModeBanner() {
		if (googleAdsUsable() && Util.isConnectedToInternet()) {
			displayGoogleEasyModeBanner();
		} else {
			displayOwnEasyModeBanner();
		}
	}

	public static void removeEasyModeBanner() {
		if (googleAdsUsable()) {
			Game.instance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					int index = bannerIndex();
					if(index>=0) {

						View adview = Game.instance().getLayout().getChildAt(index);
						if (adview instanceof BannerView) {
							Appodeal.hide(Game.instance(),Appodeal.BANNER);
						}

						Game.instance().getLayout().removeViewAt(index);
					}
				}

			});
		}
	}

	private static Map<InterstitialAd, Boolean> mAdLoadInProgress = new HashMap<>();

	private static void requestNewInterstitial(final InterstitialAd isAd) {

		Boolean loadAlreadyInProgress = mAdLoadInProgress.get(isAd);

		if (loadAlreadyInProgress != null && loadAlreadyInProgress) {
			return;
		}

		isAd.setAdListener(new AdListener() {
			@Override
			public void onAdClosed() {
				super.onAdClosed();
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				super.onAdFailedToLoad(errorCode);
				mAdLoadInProgress.put(isAd, false);
			}

			@Override
			public void onAdLoaded() {
				super.onAdLoaded();
				mAdLoadInProgress.put(isAd, false);
			}

			@Override
			public void onAdOpened() {
				super.onAdOpened();
			}

			@Override
			public void onAdLeftApplication() {
				super.onAdLeftApplication();
			}
		});

		mAdLoadInProgress.put(isAd, true);
		isAd.loadAd(makeAdRequest());

	}

	private static void displayIsAd(final InterstitialPoint work, final InterstitialAd isAd) {
		if (googleAdsUsable() && Util.isConnectedToInternet()) {
			Game.instance().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (isAd == null) {
						work.returnToWork(false);
						return;
					}

					if (!isAd.isLoaded()) {
						work.returnToWork(false);
						return;
					}

					isAd.setAdListener(new AdListener() {
						@Override
						public void onAdClosed() {
							requestNewInterstitial(isAd);
							work.returnToWork(true);
						}
					});
					isAd.show();
				}
			});
		} else {
			OwnAds.displayIsAd(work);
		}
	}

	public static void displaySaveAndLoadAd(final InterstitialPoint work) {
		displayIsAd(work, mSaveAndLoadAd);
	}

	public static void displayEasyModeSmallScreenAd(final InterstitialPoint work) {
		if (needDisplaySmallScreenEasyModeIs()) {
			displayIsAd(work, mEasyModeSmallScreenAd);
		} else {
			work.returnToWork(true);
		}
	}

	private static void initEasyModeIntersitial() {
		if (googleAdsUsable() && Util.isConnectedToInternet()) {
			{
				Game.instance().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (mEasyModeSmallScreenAd == null) {
							mEasyModeSmallScreenAd = new InterstitialAd(Game.instance());
							mEasyModeSmallScreenAd.setAdUnitId(Game.getVar(R.string.easyModeSmallScreenAdUnitId));
							requestNewInterstitial(mEasyModeSmallScreenAd);
						}
					}
				});
			}
		}
	}

	public static void initSaveAndLoadIntersitial() {
		if (googleAdsUsable() && Util.isConnectedToInternet()) {
			{
				Game.instance().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (mSaveAndLoadAd == null) {
							mSaveAndLoadAd = new InterstitialAd(Game.instance());
							mSaveAndLoadAd.setAdUnitId(Game.getVar(R.string.saveLoadAdUnitId));
							requestNewInterstitial(mSaveAndLoadAd);
						}
					}
				});
			}
		}
	}


}
