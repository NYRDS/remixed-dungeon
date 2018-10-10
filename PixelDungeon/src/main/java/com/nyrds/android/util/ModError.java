package com.nyrds.android.util;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.utils.GLog;

class ModError extends RuntimeException {
    ModError(String s, Exception e) {
        super(s,e);
        Game.toast("[%s -> %s]", s, e.getMessage());
        EventCollector.logException(e,s);
        Notifications.displayNotification(e.getClass().getSimpleName(), s, e.getMessage());
        GLog.toFile(s);
        GLog.toFile(e.getMessage());
    }
}
