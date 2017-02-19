package com.nyrds.android.util;

import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.support.GooglePlayServices;
import com.watabou.pixeldungeon.PixelDungeon;

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
				&& GooglePlayServices.googlePlayServicesUsable(PixelDungeon.instance());
	}


	public static boolean haveDonations() {
		return (BuildConfig.FLAVOR.equals(GOOGLE_PLAY) || BuildConfig.FLAVOR.equals(CHROME_WEB_STORE))
				&& GooglePlayServices.googlePlayServicesUsable(PixelDungeon.instance());


	}

	public static boolean haveAds() {
		return BuildConfig.FLAVOR.equals(GOOGLE_PLAY)
				|| BuildConfig.FLAVOR.equals(AMAZON)
				|| BuildConfig.FLAVOR.equals(YANDEX);
	}
}
