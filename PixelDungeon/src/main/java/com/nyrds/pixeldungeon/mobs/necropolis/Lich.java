package com.nyrds.pixeldungeon.mobs.necropolis;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class Lich extends Mob {
    {
        hp(ht(120));
        EXP = 20;
        defenseSkill = 20;

        IMMUNITIES.add(Paralysis.class);
        IMMUNITIES.add(ToxicGas.class);
        IMMUNITIES.add( Terror.class );
        IMMUNITIES.add( Death.class );
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange( 8, 15 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 20;
    }

    @Override
    public int dr() {
        return 5;
    }


}
