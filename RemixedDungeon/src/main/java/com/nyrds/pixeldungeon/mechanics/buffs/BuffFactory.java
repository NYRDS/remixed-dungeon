package com.nyrds.pixeldungeon.mechanics.buffs;

import com.nyrds.android.util.ModError;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.SneakyThrows;

public class BuffFactory {
    static private Map<String, Class<? extends Buff>> buffList = new HashMap<>();

    static private Set<String> predefinedCustomBuffs = new HashSet<>();

    static private LuaScript script = new LuaScript("scripts/buffs/Buffs", null);

    public static final String GASES_IMMUNITY = "GasesImmunity";

    static {
        initBuffsMap();
        predefinedCustomBuffs.add("ShieldLeft"); // buff for shield in left hand
        predefinedCustomBuffs.add(GASES_IMMUNITY);
        script.run("loadBuffs",null);
    }

    private static void registerBuffClass(Class<? extends Buff> buffClass) {
        buffList.put(buffClass.getSimpleName(), buffClass);
    }

    private static void initBuffsMap() {
        registerBuffClass(Burning.class);

    }

    private static boolean hasBuffForName(String name) {
        if(predefinedCustomBuffs.contains(name)) {
            return true;
        }

        if (buffList.get(name) != null) {
            return true;
        }
        script.run("haveBuff", name);
        return script.getResult().checkboolean();
    }

    @NotNull
    @SneakyThrows
    public static Buff getBuffByName(String name) {
        if(hasBuffForName(name)) {
            Class<? extends Buff> buffClass = buffList.get(name);
            if (buffClass == null) {
                return new CustomBuff(name);
            }
            return buffClass.newInstance();
        }

        throw new ModError(name, new Exception("Unknown Buff"));
    }
}
