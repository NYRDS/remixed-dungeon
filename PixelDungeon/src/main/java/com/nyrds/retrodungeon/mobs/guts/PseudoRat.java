package com.nyrds.retrodungeon.mobs.guts;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class PseudoRat extends Mob {
    {
        hp(ht(320));
        defenseSkill = 30;

        exp = 20;
        maxLvl = 35;

        loot = Gold.class;
        lootChance = 0.8f;

        setState(HUNTING);

        IMMUNITIES.add(Paralysis.class);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(40, 70);
    }

    @Override
    public int attackSkill( Char target ) {
        return 30;
    }

    @Override
    public int dr() {
        return 25;
    }

    @Override
    public int getKind() {
        return 1;
    }
}
