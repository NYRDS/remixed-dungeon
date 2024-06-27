package com.nyrds.pixeldungeon.windows;

import com.nyrds.LuaInterface;
import com.watabou.pixeldungeon.windows.WndOptions;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

@LuaInterface
public class WndOptionsLua extends WndOptions {

    private final LuaFunction callback;


    public WndOptionsLua(Object callback, String title, String message, String... options) {
        super(title, message, options);
        this.callback = (LuaFunction) callback;
    }

    @Override
    public void onSelect(int index) {
        callback.invoke(new LuaValue[]{CoerceJavaToLua.coerce(index)});
    }
}
