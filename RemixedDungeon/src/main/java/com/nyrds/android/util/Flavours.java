package com.nyrds.android.util;

import com.nyrds.pixeldungeon.support.Google.GooglePlayServices;
import com.watabou.pixeldungeon.RemixedDungeon;

/**
 * Created by mike on 04.06.2016.
 */
public class Flavours {
	public static boolean haveHats() {
		return GooglePlayServices.googlePlayServicesUsable(RemixedDungeon.instance());
	}

	public static boolean haveDonations() {
		return GooglePlayServices.googlePlayServicesUsable(RemixedDungeon.instance());
	}
}
