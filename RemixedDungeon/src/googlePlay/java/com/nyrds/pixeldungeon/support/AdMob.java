package com.nyrds.pixeldungeon.support;

import com.google.android.gms.ads.AdRequest;

class AdMob {

    public static AdRequest makeAdRequest() {
        /*
        if (EuConsent.getConsentLevel() < EuConsent.PERSONALIZED) {
            Bundle extras = new Bundle();
            extras.putString("npa", "1");

            return new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();
        }
        */
        return new AdRequest.Builder().build();
    }
}
