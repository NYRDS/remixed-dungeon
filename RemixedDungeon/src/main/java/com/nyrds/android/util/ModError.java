package com.nyrds.android.util;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.utils.GLog;

public class ModError extends RuntimeException {

    public ModError(String s) {
        super(s);
        doReport(s,this);
    }

    public ModError(String s, Exception e) {
        super(s,e);
        doReport(s, e);
    }

    static public void doReport(String s, Exception e) {
        String errMsg = e.getMessage();
        if(errMsg==null) {
            errMsg = "";
        }
        Game.toast("[%s -> %s]", s, errMsg);
        EventCollector.logException(e,s);
        Notifications.displayNotification(e.getClass().getSimpleName(), s, errMsg);
        GLog.toFile(s);
        GLog.toFile(errMsg);
    }
}
