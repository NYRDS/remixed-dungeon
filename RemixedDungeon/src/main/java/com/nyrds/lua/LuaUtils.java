package com.nyrds.lua;

import com.nyrds.LuaInterface;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

@LuaInterface
public class LuaUtils {
    @LuaInterface
    public static LuaTable arrayToTable(Object[] array) {
        LuaTable result = new LuaTable();

        for(int i = 0; i < array.length; ++i)
            if(array[i] instanceof Object[])
                result.set(i, arrayToTable((Object[])array[i]));
            else
                result.set(i, CoerceJavaToLua.coerce(array[i]));

        return result;
    }

}
