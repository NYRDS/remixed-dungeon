package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class YogsHeart extends Boss {
    {
        hp(ht(300));
        defenseSkill = 40;

        EXP = 12;

        loot = Gold.class;
        lootChance = 0.5f;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(25, 35);
    }

    @Override
    public int attackSkill( Char target ) {
        return 21;
    }

    @Override
    public int dr() {
        return 2;
    }
}
