package com.nyrds.pixeldungeon.mechanics;

import com.nyrds.android.lua.LuaEngine;
import com.nyrds.android.util.TrackedRuntimeException;

import org.luaj.vm2.LuaError;
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

    private void run(String method, LuaValue[] args) {
        try {
            if (!scriptLoaded) {
                script = LuaEngine.module(scriptFile, scriptFile);
                scriptLoaded = true;
            }

            if (script != null) {
                scriptResult = script.invokemethod(method, args).arg1();
            }
        } catch (LuaError e) {
            throw new TrackedRuntimeException(e.getMessage());
        }
    }

    public void run(String method, Object arg1) {
        run(method,new LuaValue[]{
                CoerceJavaToLua.coerce(parent),
                CoerceJavaToLua.coerce(arg1)});
    }

    public void run(String method, Object arg1, Object arg2) {
        run(method,new LuaValue[]{
                CoerceJavaToLua.coerce(parent),
                CoerceJavaToLua.coerce(arg1),
                CoerceJavaToLua.coerce(arg2)});
    }

    public LuaValue getResult() {
        return scriptResult;
    }
}
