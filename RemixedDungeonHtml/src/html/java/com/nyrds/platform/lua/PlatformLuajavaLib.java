package com.nyrds.platform.lua;

import com.nyrds.platform.lua.LuaClassMap;
import com.nyrds.platform.lua.LuaConstructorMap;
import com.nyrds.platform.lua.LuaMethodMap;
import com.nyrds.util.ModError;
import com.nyrds.util.ModdingBase;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.lib.jse.JavaClassHelper;
import org.luaj.vm2.lib.jse.LuajavaLib;

import java.util.HashMap;
import java.util.Map;

/**
 * HTML version of PlatformLuajavaLib
 */
public class PlatformLuajavaLib extends LuajavaLib {

    final static Map<String, String> classRemap = new HashMap<>();

    static {
        classRemap.put("com.watabou.pixeldungeon.RemixedDungeon", "com.nyrds.platform.game.RemixedDungeon");
        classRemap.put("com.watabou.noosa.audio.Sample", "com.nyrds.platform.audio.Sample");
        classRemap.put("com.watabou.noosa.audio.Music", "com.nyrds.platform.audio.MusicManager");
        classRemap.put("com.watabou.noosa.StringsManager", "com.nyrds.platform.util.StringsManager");
        classRemap.put("com.nyrds.platform.Input", "com.nyrds.platform.app.Input");
        classRemap.put("com.nyrds.platform.audio.Music","com.nyrds.platform.audio.MusicManager");
    }

    public PlatformLuajavaLib() {
        super();
    }

    @Override
    public Varargs invoke(Varargs args) {
        try {
            return super.invoke(args);
        } catch (LuaError e) {
            // TeaVM reflection may fail on newInstance for some classes
            // (static init chains, missing constructors, etc.)
            if (opcode == 2) { // NEWINSTANCE = 2 in LuajavaLib
                String className = args.checkjstring(1);
                System.err.println("PlatformLuajavaLib: newInstance failed for '" + className
                    + "': " + e.getMessage() + ". Attempting generated constructor map.");
                try {
                    // Try generated constructor map first - pass raw Lua values for proper coercion
                    LuaValue[] luaArgs = new LuaValue[args.narg() - 1];
                    for (int i = 2; i <= args.narg(); i++) {
                        luaArgs[i - 2] = args.arg(i);
                    }
                    Object instance = LuaConstructorMap.newInstance(className, luaArgs);
                    if (instance != null) {
                        return CoerceJavaToLua.coerce(instance);
                    }
                } catch (Exception ex1) {
                    System.err.println("PlatformLuajavaLib: generated constructor map failed for '"
                        + className + "': " + ex1.getMessage() + ". Falling back to direct construction.");
                }
                try {
                    Class<?> clazz = classForName(className);
                    // Try to create an actual instance via default constructor
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    return CoerceJavaToLua.coerce(instance);
                } catch (Exception ex1) {
                    System.err.println("PlatformLuajavaLib: direct construction also failed for '"
                        + className + "': " + ex1.getMessage() + ". Falling back to class proxy.");
                    // Last resort: return class proxy for static method calls
                    try {
                        Class<?> clazz2 = classForName(className);
                        return JavaClassHelper.forClass(clazz2);
                    } catch (Exception fallbackEx) {
                        throw e;
                    }
                }
            }
            // Handle method calls (opcode == 1) via generated method map
            if (opcode == 1) { // INVOKE = 1 in LuajavaLib
                String className = args.checkjstring(1);
                String methodName = args.checkjstring(2);
                System.err.println("PlatformLuajavaLib: method invoke failed for '" + className + "." + methodName
                    + "': " + e.getMessage() + ". Attempting generated method map.");
                try {
                    // Build args array from Lua values (skip className and methodName)
                    Object[] javaArgs = new Object[args.narg() - 2];
                    for (int i = 3; i <= args.narg(); i++) {
                        LuaValue lv = args.arg(i);
                        if (lv.isint()) {
                            javaArgs[i - 3] = lv.checkint();
                        } else if (lv.isnumber()) {
                            javaArgs[i - 3] = lv.checkdouble();
                        } else if (lv.isboolean()) {
                            javaArgs[i - 3] = lv.checkboolean();
                        } else if (lv.isstring()) {
                            javaArgs[i - 3] = lv.checkjstring();
                        } else if (lv.isuserdata()) {
                            javaArgs[i - 3] = lv.touserdata();
                        } else if (lv.istable()) {
                            // Convert Lua table to Object[] - best effort
                            javaArgs[i - 3] = CoerceLuaToJava.coerce(lv, Object[].class);
                        } else {
                            javaArgs[i - 3] = lv;
                        }
                    }
                    Object result = LuaMethodMap.invoke(className, methodName, javaArgs);
                    if (result != null) {
                        return CoerceJavaToLua.coerce(result);
                    }
                } catch (Exception ex1) {
                    System.err.println("PlatformLuajavaLib: generated method map failed for '"
                        + className + "." + methodName + "': " + ex1.getMessage());
                }
            }
            throw e;
        }
    }

    @Override
    protected Class classForName(String name) {
        // First try generated class map (no reflection)
        Class<?> clazz = LuaClassMap.get(name);
        if (clazz != null) {
            return clazz;
        }

        // Fallback to remap + reflection (for modded classes not in map)
        ClassLoader classLoader = this.getClass().getClassLoader();

        String actualClassName = name;

        if (classRemap.containsKey(name)) {
            actualClassName = classRemap.get(name);
        }

        try {
            Class<?> cls = Class.forName(actualClassName, true, classLoader);
            return cls;
        } catch (ClassNotFoundException e) {
            ModError.doReport("Failed to load class ["+classLoader.toString() + "] in mod "+ ModdingBase.activeMod(), e);
            return Object.class;
        }
    }
}
