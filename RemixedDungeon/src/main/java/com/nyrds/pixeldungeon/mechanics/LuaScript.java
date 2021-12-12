package com.nyrds.pixeldungeon.mechanics;

import com.nyrds.lua.LuaEngine;
import com.nyrds.platform.EventCollector;

import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

/**
 * Created by mike on 26.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class LuaScript {

    private final String scriptFile;
    private boolean asInstance = false;

    private static final LuaValue[] emptyArgs = new LuaValue[0];
    private final LuaValue[] onlyParentArgs = new LuaValue[1];

    @Nullable
    private final Object   parent;

    public LuaScript(String scriptFile, @Nullable Object parent)
    {
        this.parent = parent;
        this.scriptFile = scriptFile;
        onlyParentArgs[0] = CoerceJavaToLua.coerce(parent);
    }

    public void asInstance() {
        asInstance = true;
    }

    private LuaTable getScript() {
        EventCollector.setSessionData("script", scriptFile);

        if(asInstance) {
            return LuaEngine.moduleInstance(this, scriptFile);
        }
        return LuaEngine.require(scriptFile);
    }

    private LuaValue run(String method, LuaValue[] args) {
        return getScript().invokemethod(method, args).arg1();
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
        if(!hasMethod(method)) {
            return;
        }
        run(method);
    }

    public boolean hasMethod(String method) {
        return getScript().get(method).isfunction();
    }

    public void runOptionalNoRet(String method, Object... args) {
        runOptional(method, null, args);
    }

    public <T> T runOptional(String method, T defaultValue, Object... args) {
        if (!hasMethod(method)) {
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

        if(defaultValue==null) {
            run(method, luaArgs);
            return null;
        }

        return (T) CoerceLuaToJava.coerce(
                run(method, luaArgs),
                defaultValue.getClass());
    }
}
