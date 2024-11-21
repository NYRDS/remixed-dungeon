package com.watabou.noosa;

import com.nyrds.util.InterstitialPoint;
import com.nyrds.util.events.EventCollector;
import com.nyrds.util.Utils;

public class ReturnOnlyOnce implements InterstitialPoint {

    private final InterstitialPoint mTarget;
    private int mReturnCounter = 0;

    public ReturnOnlyOnce(InterstitialPoint target) {
        mTarget = target;
    }

    @Override
    public void returnToWork(boolean result) {
        switch (mReturnCounter) {
            case 0:
                mTarget.returnToWork(result);
            break;

            case 1:
                EventCollector.logException(Utils.format("%d return to %s", mReturnCounter, mTarget.getClass().getCanonicalName()));
            break;
        }

        mReturnCounter++;
    }
}
