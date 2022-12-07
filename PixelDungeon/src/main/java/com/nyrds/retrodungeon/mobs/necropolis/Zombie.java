package com.nyrds.retrodungeon.mobs.necropolis;

import android.support.annotation.NonNull;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class Zombie extends UndeadMob {
    {
        hp(ht(33));
        defenseSkill = 10;

        exp = 6;
        maxLvl = 15;

        loot = Gold.class;
        lootChance = 0.02f;
    }

    @Override
    public int attackProc(@NonNull Char enemy, int damage ) {
        //Poison proc
        if (Random.Int(3) == 1){
            Buff.affect( enemy, Poison.class ).set( Random.Int( 2, 4 ) * Poison.durationFactor( enemy ) );
        }

        return damage;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(3, 10);
    }

    @Override
    public int attackSkill( Char target ) {
        return 10;
    }

    @Override
    public int dr() {
        return 10;
    }


}
