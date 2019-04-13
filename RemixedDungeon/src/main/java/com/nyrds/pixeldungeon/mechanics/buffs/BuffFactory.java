package com.nyrds.pixeldungeon.mechanics.buffs;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;

import java.util.HashMap;
import java.util.Map;

public class BuffFactory {
    static private Map<String, Class<? extends Buff>> mBuffList = new HashMap<>();

    static private LuaScript script = new LuaScript("scripts/buffs/Buffs", null);

    static {
        initBuffsMap();
        script.run("loadBuffs",null);
    }

    private static void registerBuffClass(Class<? extends Buff> buffClass) {
        mBuffList.put(buffClass.getSimpleName(), buffClass);
    }

    private static void initBuffsMap() {
        registerBuffClass(Burning.class);
    }

    public static boolean hasBuffForName (String name) {
        if (mBuffList.get(name) != null) {
            return true;
        }
        script.run("haveBuff", name);
        return script.getResult().checkboolean();
    }

    public static Buff getBuffByName(String name) {
        try {
            if(hasBuffForName(name)) {
                Class<? extends Buff> buffClass = mBuffList.get(name);
                if (buffClass == null) {
                    return new CustomBuff(name);
                }
                return buffClass.newInstance();
            }
        } catch (InstantiationException e) {
            throw new TrackedRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new TrackedRuntimeException(e);
        }
        return null;
    }
}
