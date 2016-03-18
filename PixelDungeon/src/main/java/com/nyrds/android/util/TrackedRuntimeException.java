package com.nyrds.android.util;

import com.nyrds.pixeldungeon.ml.EventCollector;

/**
 * Created by DeadDie on 18.03.2016
 */
public class TrackedRuntimeException extends RuntimeException {
    public TrackedRuntimeException( String s,Exception e) {
        super(s,e);
        EventCollector.logFatalException(e,s);
    }
}
