package com.nyrds.pixeldungeon.mobs.icecaves;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

/**
 * Created by DeadDie on 12.02.2016
 */
public class ColdSpirit extends Mob {

    public ColdSpirit(){
        hp(ht(50));

        baseSpeed = 1.3f;
        baseDefenseSkill = 16;
        baseAttackSkill  = 22;
        flying = true;

        expForKill = 8;
        maxLvl = 20;
        dmgMin = 12;
        dmgMax = 15;
        dr = 22;

        loot(Gold.class, 0.02f);
    }

    @Override
    public int attackProc(@NotNull Char enemy, int damage ) {
        //Buff proc
        if (Random.Int( 4 ) == 1) {
            Freezing.affect( enemy.getPos());
        }
        return damage;
    }
}
