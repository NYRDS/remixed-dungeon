package com.nyrds.pixeldungeon.modding;

import com.nyrds.LuaInterface;
import com.watabou.pixeldungeon.RemixedDungeon;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    public static void Call(String event, Object... args) {
        for (Map.Entry<String, List<LuaClosure>> entry : RemixedDungeon.events.entrySet()) {
            String name = entry.getKey();
            List<LuaClosure> callbacks = entry.getValue();

            if (name.equals(event)) {
                Iterator<LuaClosure> it = callbacks.iterator();
                List<LuaValue> values = objectsToValues(args);

                while(it.hasNext()){
                    LuaClosure cb = (LuaClosure) it.next();
                    cb.invoke(values.toArray(new LuaValue[values.size()]));
                }
            }
        }
    }

    @LuaInterface
    public static void Add(String name, LuaClosure callback) {
        for (Map.Entry<String, List<LuaClosure>> entry : RemixedDungeon.events.entrySet()) {
            String n = entry.getKey();
            List<LuaClosure> callbacks = entry.getValue();

            if (n.equals(name)) {
                callbacks.add(callback);
                RemixedDungeon.events.put(entry.getKey(), callbacks);
            }
        }

        List<LuaClosure> callbacks = new ArrayList<>();
        callbacks.add(callback);

        RemixedDungeon.events.put(name, callbacks);
    }
}