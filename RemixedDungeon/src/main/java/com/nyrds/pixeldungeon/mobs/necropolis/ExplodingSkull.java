package com.nyrds.pixeldungeon.mobs.necropolis;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Gold;

import org.jetbrains.annotations.NotNull;

/**
 * Created by DeadDie on 12.02.2016
 */
public class ExplodingSkull extends UndeadMob {
    {
        hp(ht(10));
        baseDefenseSkill = 1;
        baseAttackSkill  = 125;

        dmgMin = 25;
        dmgMax = 45;
        dr = 1;

        baseSpeed = 1.5f;

        expForKill = 1;
        maxLvl = 1;

        loot(Gold.class, 0.02f);
    }

    @Override
    public boolean attack(@NotNull Char enemy) {
        if(super.attack(enemy)) {
            die(this);
            return true;
        }
        return false;
    }
}
