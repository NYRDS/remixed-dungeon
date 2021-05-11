package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;

/**
 * Created by DeadDie on 12.02.2016
 */
public class YogsEye extends Mob {
    {
        hp(ht(165));
        baseDefenseSkill = 25;
        baseAttackSkill  = 24;

        dmgMin = 40;
        dmgMax = 45;
        dr = 2;

        exp = 25;

        loot(Gold.class, 0.5f);
    }

    @Override
    public boolean canBePet() {
        return false;
    }
}
