package com.nyrds.pixeldungeon.support;

import com.gameofwhales.sdk.Experiment;
import com.gameofwhales.sdk.GameOfWhales;
import com.gameofwhales.sdk.GameOfWhalesListener;
import com.gameofwhales.sdk.SpecialOffer;
import com.watabou.pixeldungeon.utils.GLog;

public class TestGameOfWhalesListener implements GameOfWhalesListener {

    @Override
    public void onSpecialOfferAppeared(SpecialOffer specialOffer) {
        GLog.debug("GOW onSpecialOfferAppeared: " + specialOffer.toString());
    }

    @Override
    public void onSpecialOfferDisappeared(SpecialOffer specialOffer) {
        GLog.debug("GOW onSpecialOfferDisappeared: " + specialOffer.toString());
    }

    @Override
    public void onFutureSpecialOfferAppeared(SpecialOffer specialOffer) {
        GLog.debug("GOW onFutureSpecialOfferAppeared: " + specialOffer.toString());
    }


    @Override
    public void onPushDelivered(SpecialOffer offer, String campID, String title, String message)
    {
        GLog.debug("GOW onPushDelivered: %s %s %s", campID, title, message);
        //It's called to show notification in opened game.
    }


    @Override
    public void onInitialized() {
        GLog.debug("GOW initialized");
        //It's needed just if you want to get information that the SDK has been initialized.
    }

    @Override
    public void onConnected(boolean dataReceived){
        //It's called after the GOW server response with 'dataReceived': true.
        //If there was no response from the GOW server, there was an error during the request to the server
        //or the game is offline, 'dataReceived' is false.
        GLog.debug("GOW connected: %b", dataReceived);
    }

    @Override
    public void onPurchaseVerified(final String transactionID, final String state) {
        if (state.equals(GameOfWhales.VERIFY_STATE_ILLEGAL))
        {
            //TODO: Refund money if state is illegal
        }
        GLog.debug("GOW onPurchaseVerified: %s %s", transactionID, state);
    }

    @Override
    public void onAdLoaded() {
        GLog.debug("GOW onAdLoaded");
    }

    @Override
    public void onAdLoadFailed() {
        GLog.debug("GOW onAdLoadFailed");
    }

    @Override
    public void onAdClosed() {
        GLog.debug("GOW onAdLoadClosed");
    }

    @Override
    public boolean CanStartExperiment(Experiment experiment) {
        GLog.debug("GOW canStartExperiment: %s", experiment.toString());
        return false;
    }

    @Override
    public void OnExperimentEnded(Experiment experiment) {
        GLog.debug("GOW onExperimentLoad: %s", experiment.toString());
    }
}
