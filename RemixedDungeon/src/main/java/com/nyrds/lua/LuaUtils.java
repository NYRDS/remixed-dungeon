package com.nyrds.lua;

import com.nyrds.LuaInterface;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.Collection;

@LuaInterface
public class LuaUtils {
    @LuaInterface
    public static LuaTable arrayToTable(Object[] array) {
        LuaTable result = new LuaTable();

        for(int i = 0; i < array.length; ++i)
            if(array[i] instanceof Object[])
                result.set(i+1, arrayToTable((Object[])array[i]));
            else if(array[i] instanceof Collection)
                result.set(i+1, CollectionToTable((Collection<? extends Object>) array[i]));
            else
                result.set(i+1, CoerceJavaToLua.coerce(array[i]));

        return result;
    }

    @LuaInterface
    public static LuaTable CollectionToTable(Collection<? extends Object> collection) {
        return arrayToTable(collection.toArray());
    }
}
