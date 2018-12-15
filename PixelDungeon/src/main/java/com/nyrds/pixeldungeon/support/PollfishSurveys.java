package com.nyrds.pixeldungeon.support;

import android.os.Build;

import com.nyrds.pixeldungeon.ml.R;
import com.pollfish.classes.SurveyInfo;
import com.pollfish.interfaces.PollfishCompletedSurveyListener;
import com.pollfish.interfaces.PollfishReceivedSurveyListener;
import com.pollfish.interfaces.PollfishSurveyNotAvailableListener;
import com.pollfish.interfaces.PollfishUserNotEligibleListener;
import com.pollfish.main.PollFish;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.utils.GLog;

public class PollfishSurveys {

    private static boolean allowed() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean init() {
        if (allowed()) {
            PollFish.ParamsBuilder params = new PollFish.ParamsBuilder(Game.getVar(R.string.pollfish_key))
                    .customMode(true)
                    .pollfishReceivedSurveyListener(new PollfishReceivedSurveyListener() {
                        @Override
                        public void onPollfishSurveyReceived(SurveyInfo surveyInfo) {
                            GLog.w(surveyInfo.toString());
                        }
                    })
                    .pollfishUserNotEligibleListener(new PollfishUserNotEligibleListener() {
                        @Override
                        public void onUserNotEligible() {
                            GLog.w("bad user");
                        }
                    })
                    .pollfishCompletedSurveyListener(new PollfishCompletedSurveyListener() {
                        @Override
                        public void onPollfishSurveyCompleted(SurveyInfo surveyInfo) {
                            GLog.w(surveyInfo.toString());
                        }
                    })
                    .pollfishSurveyNotAvailableListener(new PollfishSurveyNotAvailableListener() {
                        @Override
                        public void onPollfishSurveyNotAvailable() {
                            GLog.w("no survey");
                        }
                    })
                    .build();
            PollFish.initWith(Game.instance(), params);
            PollFish.hide();


        }
        return false;
    }

}
