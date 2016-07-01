package com.nyrds.pixeldungeon.mobs.necropolis;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class DeathKnight extends Mob {
    {
        hp(ht(45));
        defenseSkill = 17;

        EXP = 10;
        maxLvl = 15;

        loot = Gold.class;
        lootChance = 0.02f;

        IMMUNITIES.add(Paralysis.class);
        IMMUNITIES.add(ToxicGas.class);
        IMMUNITIES.add( Terror.class );
        IMMUNITIES.add( Death.class );
    }

    @Override
    public int attackProc( Char enemy, int damage ) {

        //Paralysis proc
        if (Random.Int(10) == 1){
            Buff.affect(enemy, Paralysis.class);
        }
        return damage;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(7, 14);
    }

    @Override
    public int attackSkill( Char target ) {
        return 25;
    }

    @Override
    public int dr() {
        return 7;
    }


}
