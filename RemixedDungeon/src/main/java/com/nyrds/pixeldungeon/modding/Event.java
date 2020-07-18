package com.nyrds.pixeldungeon.modding;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaValue;

public class Event {
    public String event;
    public LuaClosure action;

    Event(String event, LuaClosure action) {
        this.event = event;
        this.action = action;
    }

    public void execute(Object... args) {
        this.action.invoke((LuaValue[]) args);
    }
}