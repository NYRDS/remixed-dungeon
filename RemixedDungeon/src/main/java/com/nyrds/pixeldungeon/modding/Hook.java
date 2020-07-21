package com.nyrds.pixeldungeon.modding;

import com.nyrds.LuaInterface;
import com.watabou.pixeldungeon.RemixedDungeon;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static void Remove(String event, String name) {
        if(!RemixedDungeon.events.containsKey(event)) {
            return;
        }

        Map<String, LuaClosure> hookMap = RemixedDungeon.events.get(event);
        if(hookMap.isEmpty()) {
            return;
        }

        if(hookMap.get(name) == null) {
            return;
        }

        hookMap.remove(name);
    }

    @LuaInterface
    public static void Call(String event, Object... args) {

        if(!RemixedDungeon.events.containsKey(event)) {
            return;
        }

        Map<String, LuaClosure> hookMap = RemixedDungeon.events.get(event);

        if(hookMap.isEmpty()) {
            return;
        }

        LuaValue[] argsList = objectsToValues(args).toArray(new LuaValue[0]);

        for (Map.Entry<String, LuaClosure> entry: hookMap.entrySet()) {
            entry.getValue().invoke(argsList);
        }
    }

    @LuaInterface
    public static void Add(String event, String name, LuaClosure callback) {

        if(!RemixedDungeon.events.containsKey(event)) {
            RemixedDungeon.events.put(event, new HashMap<>());
        }

        RemixedDungeon.events.get(event).put(name, callback);
    }
}