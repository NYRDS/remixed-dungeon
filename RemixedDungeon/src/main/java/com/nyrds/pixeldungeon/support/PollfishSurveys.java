package com.nyrds.pixeldungeon.support;

import android.os.Build;

import com.nyrds.pixeldungeon.ml.R;
import com.pollfish.classes.SurveyInfo;
import com.pollfish.constants.SurveyFormat;
import com.pollfish.interfaces.PollfishClosedListener;
import com.pollfish.interfaces.PollfishCompletedSurveyListener;
import com.pollfish.interfaces.PollfishOpenedListener;
import com.pollfish.interfaces.PollfishReceivedSurveyListener;
import com.pollfish.interfaces.PollfishSurveyNotAvailableListener;
import com.pollfish.interfaces.PollfishUserNotEligibleListener;
import com.pollfish.interfaces.PollfishUserRejectedSurveyListener;
import com.pollfish.main.PollFish;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Preferences;
import com.watabou.pixeldungeon.utils.GLog;

public class PollfishSurveys {


    private static boolean haveReview;
    private static boolean allowed() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static void initIfAllowed() {
        if(allowed() && Preferences.INSTANCE.checkBoolean("PollfishSurveys")){
            init();
        }
    }

    public static boolean init() {
        if (allowed()) {
            Preferences.INSTANCE.put("PollfishSurveys", true);

            PollFish.ParamsBuilder params = new PollFish.ParamsBuilder(Game.getVar(R.string.pollfish_key))
                    //.customMode(true)
                    .releaseMode(false)
                    .surveyFormat(SurveyFormat.BASIC)
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
                    }).pollfishUserRejectedSurveyListener(new PollfishUserRejectedSurveyListener() {
                        @Override
                        public void onUserRejectedSurvey() {
                            GLog.w("rejected");
                        }
                    }).pollfishOpenedListener(new PollfishOpenedListener() {
                        @Override
                        public void onPollfishOpened() {
                            GLog.w("opened");
                        }
                    }).pollfishClosedListener(new PollfishClosedListener() {
                        @Override
                        public void onPollfishClosed() {
                            GLog.w("closed");
                        }
                    })
                    .build();


            PollFish.initWith(Game.instance(), params);
            //PollFish.hide();
        }
        return false;
    }

    public static void showSurvey() {
        if(PollFish.isPollfishPresent()) {
            GLog.p("we have survey");
            PollFish.show();
            return;
        }

        GLog.n("No survey this time");

    }

    public static boolean consented() {
        return true;
    }
}
