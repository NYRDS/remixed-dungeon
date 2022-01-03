package com.nyrds.util;

import com.nyrds.lua.LuaEngine;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.app.Notifications;
import com.nyrds.platform.game.Game;
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

            String[] messages = e.getMessage().split(LuaEngine.traceback.DetailsSeparator);

            String[] elements = messages[0].split("\n\t");
            for (int i = elements.length-2;i>0;i--) {

                String[] fields = elements[i].split(":");

                String file = fields[0];
                int lineNumber = Integer.parseInt(fields[1]);

                fields = elements[i].split("function");
                String method = "unknown";

                if(fields.length > 1 && fields[1] != null) {
                    method = fields[1].replace('\'',' ').trim();
                }

                stackTraceElements.add(0,new StackTraceElement(file,
                                                method,
                                                file,
                                                lineNumber));
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
