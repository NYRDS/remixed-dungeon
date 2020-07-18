package com.nyrds.pixeldungeon.modding;

import android.util.Log;

import com.nyrds.LuaInterface;
import com.watabou.pixeldungeon.RemixedDungeon;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@LuaInterface
public class Hook {
    private List objectsToValues(List<Object> objects) {
        List<LuaValue> values = new ArrayList<LuaValue>();

        for (Object object : objects) {
            values.add(CoerceJavaToLua.coerce(object));
        }

        return values;
    }

    @LuaInterface
    public void Call(String event, Object... args) {
        List<Object> objects = new ArrayList<Object>(args.length);
        for(Object i: args) objects.add(i);

        for (Event ev : RemixedDungeon.events) {
            if(ev.event.equals(event)) {
                List<LuaValue> values = objectsToValues(objects);

                ev.execute(values.toArray(new LuaValue[values.size()]));
            }
        }
    }

    @LuaInterface
    public void Add(String name, LuaClosure callback) {
        RemixedDungeon.events.add(new Event(name, callback));
    }
}
