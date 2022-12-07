package com.nyrds.android.util;

import com.nyrds.retrodungeon.ml.BuildConfig;
import com.nyrds.retrodungeon.support.GooglePlayServices;
import com.watabou.pixeldungeon.PixelDungeon;

/**
 * Created by mike on 04.06.2016.
 */
public class Flavours {

	private static final String GOOGLE_PLAY       = "googlePlay";
	private static final String GOOGLE_PLAY_RETRO = "googlePlayRetro";

	public static boolean haveHats() {
		return ( BuildConfig.FLAVOR.equals(GOOGLE_PLAY) || BuildConfig.FLAVOR.equals(GOOGLE_PLAY_RETRO) )
				&& GooglePlayServices.googlePlayServicesUsable(PixelDungeon.instance());
	}

	public static boolean haveDonations() {
		return (BuildConfig.FLAVOR.equals(GOOGLE_PLAY) || BuildConfig.FLAVOR.equals(GOOGLE_PLAY_RETRO))
				&& GooglePlayServices.googlePlayServicesUsable(PixelDungeon.instance());


	}

	public static boolean haveAds() {
		return BuildConfig.FLAVOR.equals(GOOGLE_PLAY)
				|| BuildConfig.FLAVOR.equals(GOOGLE_PLAY_RETRO);
	}
}
