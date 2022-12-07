package com.nyrds.retrodungeon.support;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by mike on 19.02.2017.
 * This file is part of Remixed Pixel Dungeon.
 */
public class GooglePlayServices {
	public static boolean googlePlayServicesUsable(Context context) {
	    return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
	}
}
