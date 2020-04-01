package com.nyrds.pixeldungeon.mechanics.buffs;

import com.nyrds.android.util.ModError;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Levitation;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.utils.Utils;

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

        script.run("loadBuffs");

        for(String itemFile: ModdingMode.listResources("scripts/buffs", (dir, name) -> name.endsWith(".lua"))) {
            predefinedCustomBuffs.add(itemFile.replace(".lua", Utils.EMPTY_STRING));
        }
    }

    private static void registerBuffClass(Class<? extends Buff> buffClass) {
        buffList.put(buffClass.getSimpleName(), buffClass);
    }

    private static void initBuffsMap() {
        registerBuffClass(Burning.class);
        registerBuffClass(Bleeding.class);
        registerBuffClass(Levitation.class);
        registerBuffClass(Sleep.class);

    }

    private static boolean hasBuffForName(String name) {
        if(predefinedCustomBuffs.contains(name)) {
            return true;
        }

        if (buffList.get(name) != null) {
            return true;
        }

        return script.runOptional("haveBuff", false, name);
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

        throw new ModError(name, new Exception("Unknown Buff:"+name));
    }
}
