package com.nyrds.android.util;

import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.support.Google.GooglePlayServices;
import com.watabou.pixeldungeon.RemixedDungeon;

/**
 * Created by mike on 04.06.2016.
 */
public class Flavours {

	private static final String GOOGLE_PLAY       = "googlePlay";
	private static final String GOOGLE_PLAY_RETRO = "googlePlayRetro";

	public static boolean haveHats() {
		return GooglePlayServices.googlePlayServicesUsable(RemixedDungeon.instance());
	}

	public static boolean haveDonations() {
		return GooglePlayServices.googlePlayServicesUsable(RemixedDungeon.instance());


	}

	public static boolean haveAds() {
		return BuildConfig.FLAVOR.equals(GOOGLE_PLAY)
				|| BuildConfig.FLAVOR.equals(GOOGLE_PLAY_RETRO);
	}
}
