package com.nyrds.pixeldungeon.mobs.necropolis;

import com.nyrds.android.util.TrackedRuntimeException;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class ExplodingSkull extends UndeadMob {
    {
        hp(ht(1));
        defenseSkill = 1;

        EXP = 1;
        maxLvl = 1;

        loot = Gold.class;
        lootChance = 0.02f;
    }

    @Override
    public int attackProc( Char enemy, int damage ) {

        try {

            die(this);

        } catch (Exception e) {
            throw new TrackedRuntimeException(e);
        }
        return damage;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(30, 50);
    }

    @Override
    public int attackSkill( Char target ) {
        return 125;
    }

    @Override
    public int dr() {
        return 1;
    }


}
