package com.nyrds.market;

import com.nyrds.market.GooglePlayServices;
import com.nyrds.platform.game.RemixedDungeon;

/**
 * Created by mike on 04.06.2016.
 */
public class MarketOptions {
	public static boolean haveHats() {
		return GooglePlayServices.googlePlayServicesUsable(RemixedDungeon.instance());
	}

	public static boolean haveDonations() {
		return GooglePlayServices.googlePlayServicesUsable(RemixedDungeon.instance());
	}
}
