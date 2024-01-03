package com.nyrds.pixeldungeon.mobs.necropolis;

import com.nyrds.pixeldungeon.effects.DeathStroke;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

/**
 * Created by DeadDie on 12.02.2016
 */
public class DeathKnight extends UndeadMob {
    {
        hp(ht(35));
        baseDefenseSkill = 12;
        baseAttackSkill  = 15;

        dmgMin = 7;
        dmgMax = 14;
        dr = 7;

        expForKill = 7;
        maxLvl = 15;

        loot(Gold.class, 0.02f);
    }

    @Override
    public int attackProc(@NotNull Char enemy, int damage ) {
        //Double damage proc
        if (Random.Int(7) == 1){
            if (enemy !=null){
                DeathStroke.hit(enemy);
            }
            return damage * 2;
        }
        return damage;
    }
}
