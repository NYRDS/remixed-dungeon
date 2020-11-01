package com.nyrds.android.util;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.utils.GLog;

import org.luaj.vm2.LuaError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModError extends RuntimeException {

    public ModError(String s) {
        super(s);
        doReport(s,this);
    }

    public ModError(String s, Exception e) {
        super(s,e);

        if(e instanceof LuaError) {
            List<StackTraceElement> stackTraceElements = new ArrayList<>(Arrays.asList(getStackTrace()));

            String[] elements = e.getMessage().split("\n\t");
            for (int i = elements.length-2;i>0;i--) {

                String[] fields = elements[i].split(":");

                stackTraceElements.add(0,new StackTraceElement("lua",
                                                "call",
                                                            fields[0],
                                                            Integer.valueOf(fields[1])));
            }
            setStackTrace(stackTraceElements.toArray(new StackTraceElement[0]));
        }

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
