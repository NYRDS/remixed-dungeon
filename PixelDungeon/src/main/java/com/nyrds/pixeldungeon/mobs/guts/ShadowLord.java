package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 13.02.2016
 */
public class ShadowLord extends Mob {
    {
        hp(ht(260));
        defenseSkill = 34;

        EXP = 56;

        lootChance = 0.5f;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(20, 60);
    }

    @Override
    public int attackSkill( Char target ) {
        return 36;
    }

    @Override
    public int dr() {
        return 2;
    }

}
