package com.nyrds.pixeldungeon.support;

import android.content.Context;

import com.google.ads.consent.AdProvider;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Preferences;

import java.util.List;

/**
 * Created by mike on 21.06.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class EuConsent {

    public static final int UNKNOWN          = -1;
    public static final int NON_PERSONALIZED = 0;
    public static final int PERSONALIZED     = 1;

    static List<AdProvider> providerList;

    static public void check(final Context context) {
        if (getConsentLevel() < 0) {

            final ConsentInformation consentInformation = ConsentInformation.getInstance(context);

            ConsentInformation.getInstance(context).
                    setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);


            String[] publisherIds = {Game.getVar(R.string.admob_publisher_id)};
            consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
                @Override
                public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                    if (!ConsentInformation.getInstance(context).isRequestLocationInEeaOrUnknown()) {
                        setConsentLevel(PERSONALIZED);
                    }

                    providerList = consentInformation.getAdProviders();
                }

                @Override
                public void onFailedToUpdateConsentInfo(String errorDescription) {
                    EventCollector.logEvent("eu_consent", errorDescription);
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
                        .setConsentStatus(ConsentStatus.NON_PERSONALIZED);
            break;
        }

        Preferences.INSTANCE.put(Preferences.KEY_EU_CONSENT_LEVEL, level);
    }

    static public int getConsentLevel() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_EU_CONSENT_LEVEL, UNKNOWN);
    }

}
