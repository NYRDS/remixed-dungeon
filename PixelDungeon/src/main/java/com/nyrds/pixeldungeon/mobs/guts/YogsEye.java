package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class YogsEye extends Mob {
    {
        hp(ht(165));
        defenseSkill = 25;

        EXP = 25;

        loot = Gold.class;
        lootChance = 0.5f;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(40, 45);
    }

    @Override
    public int attackSkill( Char target ) {
        return 24;
    }

    @Override
    public int dr() {
        return 2;
    }
}
