package com.nyrds.pixeldungeon.mobs.necropolis;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

/**
 * Created by DeadDie on 12.02.2016
 */
public class Zombie extends UndeadMob {
    {
        hp(ht(33));
        baseDefenseSkill = 10;
        baseAttackSkill  = 10;
        dmgMin = 3;
        dmgMax = 10;
        dr = 10;

        expForKill = 6;
        maxLvl = 15;

        loot(Gold.class, 0.02f);
    }

    @Override
    public int attackProc(@NotNull Char enemy, int damage ) {
        //Poison proc
        if (Random.Int(3) == 1){
            Buff.affect( enemy, Poison.class, Random.Int( 2, 4 ) * Poison.durationFactor( enemy ) );
        }

        return damage;
    }
}
