package com.nyrds.platform.lua;

import com.nyrds.util.ModError;
import com.nyrds.util.ModdingBase;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
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
                    + "': " + e.getMessage() + ". Attempting direct construction.");
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
            throw e;
        }
    }

    @Override
    protected Class classForName(String name) {
        ClassLoader classLoader = this.getClass().getClassLoader();

        String actualClassName = name;

        if (classRemap.containsKey(name)) {
            actualClassName = classRemap.get(name);
        }

        try {
            Class clazz = Class.forName(actualClassName, true, classLoader);
            return clazz;
        } catch (ClassNotFoundException e) {
            ModError.doReport("Failed to load class ["+classLoader.toString() + "] in mod "+ ModdingBase.activeMod(), e);
            return Object.class;
        }
    }
}
