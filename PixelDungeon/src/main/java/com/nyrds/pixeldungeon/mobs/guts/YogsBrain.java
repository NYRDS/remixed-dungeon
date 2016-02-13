package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class YogsBrain extends Mob {
    {
        hp(ht(100));
        defenseSkill = 10;

        EXP = 25;

        loot = Gold.class;
        lootChance = 0.5f;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(2, 5);
    }

    @Override
    public int attackSkill( Char target ) {
        return 11;
    }

    @Override
    public int dr() {
        return 2;
    }

    @Override
    public void die( Object cause ) {
        Ghost.Quest.process( getPos() );
        super.die( cause );
    }
}
