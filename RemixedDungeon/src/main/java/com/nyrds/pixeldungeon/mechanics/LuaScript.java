package com.nyrds.pixeldungeon.mechanics;

import com.nyrds.android.lua.LuaEngine;
import com.nyrds.android.util.ModError;

import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

/**
 * Created by mike on 26.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class LuaScript {

    private String scriptFile;
    private boolean scriptLoaded = false;
    private boolean asInstance = false;
    private LuaTable script;
    private String latestMethod;

    private static final LuaValue[] emptyArgs = new LuaValue[0];
    private final LuaValue[] onlyParentArgs = new LuaValue[1];

    @Nullable
    private Object   parent;

    private LuaValue scriptResult;

    public LuaScript(String scriptFile, @Nullable Object parent)
    {
        this.parent = parent;
        this.scriptFile = scriptFile;
        onlyParentArgs[0] = CoerceJavaToLua.coerce(parent);
    }

    public void asInstance() {
        asInstance = true;
    }

    private LuaValue run(String method, LuaValue[] args) {
        try {
            if (!scriptLoaded) {
                if(asInstance) {
                    script = LuaEngine.moduleInstance(scriptFile);
                } else {
                    script = LuaEngine.module(scriptFile);
                }
                scriptLoaded = true;
            }

            if (script != null) {
                latestMethod = method;
                return scriptResult = script.invokemethod(method, args).arg1();
            }
            throw new ModError("Can't load "+scriptFile, new Exception());
        } catch (Exception e) {
            throw new ModError("Error in "+scriptFile+"."+method,e);
        }
    }

    public LuaValue run(String method, Object arg1) {
        if(parent!=null) {
            return run(method, new LuaValue[]{
                    CoerceJavaToLua.coerce(parent),
                    CoerceJavaToLua.coerce(arg1)});
        } else {
            return run(method, new LuaValue[]{
                    CoerceJavaToLua.coerce(arg1)});
        }
    }

    public LuaValue run(String method, Object arg1, Object arg2) {
        if(parent!=null) {
            return run(method, new LuaValue[]{
                    CoerceJavaToLua.coerce(parent),
                    CoerceJavaToLua.coerce(arg1),
                    CoerceJavaToLua.coerce(arg2)});
        } else {
            return run(method, new LuaValue[]{
                    CoerceJavaToLua.coerce(arg1),
                    CoerceJavaToLua.coerce(arg2)});
        }
    }

    public LuaValue run(String method, Object arg1, Object arg2, Object arg3) {
        return run(method,new LuaValue[]{
                CoerceJavaToLua.coerce(parent),
                CoerceJavaToLua.coerce(arg1),
                CoerceJavaToLua.coerce(arg2),
                CoerceJavaToLua.coerce(arg3)});
    }

    public LuaValue run(String method) {
        if(parent==null) {
            return run(method, emptyArgs);
        } else {
            return run(method, onlyParentArgs);
        }
    }

    public void runOptional(String method) {
        if(!script.get(method).isfunction()) {
            return;
        }
        run(method);
    }

    public <T> T runOptional(String method, T defaultValue, Object... args) {
        try {
            if (!script.get(method).isfunction()) {
                return defaultValue;
            }

            int startIndex = 1;

            if(parent==null) {
                startIndex = 0;
            }

            LuaValue []luaArgs = new LuaValue[args.length+startIndex];

            if(parent!=null) {
                luaArgs[0] = CoerceJavaToLua.coerce(parent);
            }


            for (int i = startIndex;i<luaArgs.length;++i) {
                luaArgs[i] = CoerceJavaToLua.coerce(args[i-startIndex]);
            }

            return (T) CoerceLuaToJava.coerce(
                    run(method, luaArgs),
                    defaultValue.getClass());
        } catch (LuaError e) {
            throw new ModError("Error when call:" + latestMethod+"@"+scriptFile);
        }
    }
}
