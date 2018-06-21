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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by mike on 21.06.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class EuConsent {

    static List<AdProvider> providerList;

    static public void check(final Context context) {
        //if(Preferences.INSTANCE.getInt(Preferences.KEY_EU_CONSENT_LEVEL,-1) < 0) {

        final ConsentInformation consentInformation = ConsentInformation.getInstance(context);

        ConsentInformation.getInstance(context).
                setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);


        String[] publisherIds = {Game.getVar(R.string.admob_publisher_id)};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                if (!ConsentInformation.getInstance(context).isRequestLocationInEeaOrUnknown()) {
                    Preferences.INSTANCE.put(Preferences.KEY_EU_CONSENT_LEVEL, 100);
                }

                URL privacyUrl = null;
                try {
                    // TODO: Replace with your app's privacy policy URL.
                    privacyUrl = new URL("https://www.your.com/privacyurl");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    // Handle error.
                }

                providerList = consentInformation.getAdProviders();
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                EventCollector.logEvent("eu_consent", errorDescription);
            }
        });
        //}
    }

}
