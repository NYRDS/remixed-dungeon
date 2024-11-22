package com.nyrds.pixeldungeon.support;


import android.content.Context;

import com.nyrds.platform.storage.Preferences;

/**
 * Created by mike on 21.06.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class EuConsent {

    public static final int UNKNOWN          = -1;
    public static final int NON_PERSONALIZED = 0;
    public static final int PERSONALIZED     = 1;

    static public void check(final Context context) {
        setConsentLevel(NON_PERSONALIZED);
    }

    static public void setConsentLevel(int level) {

        Preferences.INSTANCE.put(Preferences.KEY_EU_CONSENT_LEVEL, level);
    }

    static public int getConsentLevel() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_EU_CONSENT_LEVEL, UNKNOWN);
    }

}
