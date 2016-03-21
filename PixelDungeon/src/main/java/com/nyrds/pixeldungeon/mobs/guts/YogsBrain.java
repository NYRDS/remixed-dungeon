package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class YogsBrain extends Boss {
    {
        hp(ht(200));
        defenseSkill = 30;

        EXP = 25;

        loot = Gold.class;
        lootChance = 0.5f;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(20, 40);
    }

    @Override
    public int attackSkill( Char target ) {
        return 31;
    }

    @Override
    public int dr() {
        return 2;
    }
}
