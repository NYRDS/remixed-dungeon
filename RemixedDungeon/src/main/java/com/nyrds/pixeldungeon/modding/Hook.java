package com.nyrds.pixeldungeon.modding;

import com.nyrds.LuaInterface;
import com.watabou.pixeldungeon.RemixedDungeon;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.ArrayList;
import java.util.List;

import lombok.var;

@LuaInterface
public class Hook {
    private static List<LuaValue> objectsToValues(Object... objects) {
        List<LuaValue> values = new ArrayList<>();

        for (Object object : objects) {
            values.add(CoerceJavaToLua.coerce(object));
        }

        return values;
    }

    @LuaInterface
    public static void Call(String event, Object... args) {

        if(!RemixedDungeon.events.containsKey(event)) {
            return;
        }

        var hookList = RemixedDungeon.events.get(event);

        if(hookList.isEmpty()) {
            return;
        }

        var argsList = objectsToValues(args).toArray(new LuaValue[0]);

        for (var hook: hookList) {
            hook.invoke(argsList);
        }
    }

    @LuaInterface
    public static void Add(String event, LuaClosure callback) {

        if(!RemixedDungeon.events.containsKey(event)) {
            RemixedDungeon.events.put(event, new ArrayList<>());
        }

        RemixedDungeon.events.get(event).add(callback);
    }
}