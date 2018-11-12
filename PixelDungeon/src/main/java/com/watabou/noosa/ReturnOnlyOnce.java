package com.watabou.noosa;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.pixeldungeon.utils.Utils;

public class ReturnOnlyOnce implements InterstitialPoint {

    InterstitialPoint mTarget;
    int mReturnCounter = 0;

    public ReturnOnlyOnce(InterstitialPoint target) {
        mTarget = target;
    }

    @Override
    public void returnToWork(boolean result) {
        if(mReturnCounter == 0) {
            mTarget.returnToWork(result);
        } else {
            EventCollector.logException(new Exception(Utils.format("%d return to %s", mReturnCounter, mTarget.getClass().getCanonicalName())));
        }
        mReturnCounter++;
    }
}
