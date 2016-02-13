package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class YogsTeeth extends Mob {
    {
        hp(ht(60));
        defenseSkill = 24;

        EXP = 16;

        loot = Gold.class;
        lootChance = 0.5f;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(10, 50);
    }

    @Override
    public int attackSkill( Char target ) {
        return 26;
    }

    @Override
    public int dr() {
        return 2;
    }
}
