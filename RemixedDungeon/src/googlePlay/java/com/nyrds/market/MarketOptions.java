package com.nyrds.market;

import com.nyrds.platform.RemoteConfig;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.game.RemixedDungeon;

/**
 * Created by mike on 04.06.2016.
 */
public class MarketOptions {
	public static boolean haveHats() {
		return GooglePlayServices.googlePlayServicesUsable(RemixedDungeon.instance()) && RemoteConfig.getInstance(RemixedDungeonApp.getContext()).getBoolean("GoogleIapEnabled", false);
	}

	public static boolean haveDonations() {
		return GooglePlayServices.googlePlayServicesUsable(RemixedDungeon.instance()) && RemoteConfig.getInstance(RemixedDungeonApp.getContext()).getBoolean("GoogleIapEnabled", false);
	}
}
