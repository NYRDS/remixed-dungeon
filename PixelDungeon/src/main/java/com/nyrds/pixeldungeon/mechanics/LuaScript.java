package com.nyrds.pixeldungeon.mechanics;

import com.nyrds.android.lua.LuaEngine;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

/**
 * Created by mike on 26.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class LuaScript {

    private String scriptFile;
    private boolean scriptLoaded = false;
    private LuaTable script;
    private Object   parent;

    private LuaValue scriptResult;

    public LuaScript(String scriptFile, Object parent)
    {
        this.parent = parent;
        this.scriptFile = scriptFile;
    }

    public boolean run(String method, Object arg1, Object arg2) {
        if (!scriptLoaded) {
            script = LuaEngine.module(scriptFile, scriptFile);
            scriptLoaded = true;
        }

        if (script != null) {
            scriptResult = script.invokemethod(method, new LuaValue[]{
                    CoerceJavaToLua.coerce(parent),
                    CoerceJavaToLua.coerce(arg1),
                    CoerceJavaToLua.coerce(arg2)})
                    .arg1();

            if (scriptResult.isboolean()) {
                return scriptResult.checkboolean();
            }
            return true;
        }
        return false;
    }

    public LuaValue getResult() {
        return scriptResult;
    }
}
