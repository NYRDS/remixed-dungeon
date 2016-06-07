package com.nyrds.android.util;

import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.support.Iap;

/**
 * Created by mike on 04.06.2016.
 */
public class Flavours {

	public static final String CHROME_WEB_STORE = "ChromeWebStore";
	public static final String AMAZON           = "Amazon";
	public static final String YANDEX           = "Yandex";
	public static final String GOOGLE_PLAY      = "GooglePlay";

	public static boolean haveHats() {
		return BuildConfig.FLAVOR.equals(GOOGLE_PLAY)
				&& Iap.googleIapUsable();
	}


	public static boolean haveDonations() {
		return (BuildConfig.FLAVOR.equals(GOOGLE_PLAY) || BuildConfig.FLAVOR.equals(CHROME_WEB_STORE))
				&& Iap.googleIapUsable();


	}

	public static boolean haveAds() {
		return BuildConfig.FLAVOR.equals(GOOGLE_PLAY)
				|| BuildConfig.FLAVOR.equals(AMAZON)
				|| BuildConfig.FLAVOR.equals(YANDEX);
	}
}
