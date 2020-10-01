package com.nyrds.pixeldungeon.support;

import android.content.Context;

import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Preferences;

/**
 * Created by mike on 21.06.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class EuConsent {

    public static final int UNKNOWN          = -1;
    public static final int NON_PERSONALIZED = 0;
    public static final int PERSONALIZED     = 1;

    static public void check(final Context context) {
        if (getConsentLevel() < NON_PERSONALIZED) {

            final ConsentInformation consentInformation = ConsentInformation.getInstance(context);


            String[] publisherIds = {Game.getVar(R.string.admob_publisher_id)};
            consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
                @Override
                public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                    if (!ConsentInformation.getInstance(context).isRequestLocationInEeaOrUnknown()) {
                        setConsentLevel(PERSONALIZED);
                    }
                }

                @Override
                public void onFailedToUpdateConsentInfo(String errorDescription) {
                    EventCollector.logException(errorDescription);
                }
            });
        }
    }

    static public void setConsentLevel(int level) {

        switch (level){
            case NON_PERSONALIZED:
                ConsentInformation.getInstance(Game.instance())
                        .setConsentStatus(ConsentStatus.NON_PERSONALIZED);
            break;
            case PERSONALIZED:
                ConsentInformation.getInstance(Game.instance())
                        .setConsentStatus(ConsentStatus.PERSONALIZED);
            break;
        }

        Preferences.INSTANCE.put(Preferences.KEY_EU_CONSENT_LEVEL, level);
    }

    static public int getConsentLevel() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_EU_CONSENT_LEVEL, UNKNOWN);
    }

}
