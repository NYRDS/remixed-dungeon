package com.nyrds.pixeldungeon.modding;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class Event {
    public String event;
    private LuaClosure action;

    Event(String event, LuaClosure action) {
        this.event = event;
        this.action = action;
    }

    public Varargs execute(LuaValue... args) {
        return this.action.invoke(args);
    }
}