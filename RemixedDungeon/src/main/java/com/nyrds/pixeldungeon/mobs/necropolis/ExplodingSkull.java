package com.nyrds.pixeldungeon.mobs.necropolis;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

/**
 * Created by DeadDie on 12.02.2016
 */
public class ExplodingSkull extends UndeadMob {
    {
        hp(ht(10));
        baseDefenseSkill = 1;
        baseAttackSkill  = 125;

        baseSpeed = 1.5f;

        exp = 1;
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

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(25, 45);
    }

    @Override
    public int dr() {
        return 1;
    }


}
