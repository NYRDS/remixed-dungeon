package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class WereratTransformed extends Mob {
    {
        hp(ht(300));
        defenseSkill = 35;

        EXP = 35;
        maxLvl = 31;

        loot = Gold.class;
        lootChance = 0.2f;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(40, 55);
    }

    @Override
    public int attackSkill( Char target ) {
        return 35;
    }

    @Override
    public int dr() {
        return 30;
    }

    @Override
    public int getKind() {
        return 1;
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        
    }
}
