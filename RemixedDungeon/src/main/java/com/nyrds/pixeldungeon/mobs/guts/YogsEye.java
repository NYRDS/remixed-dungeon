package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class YogsEye extends Mob {
    {
        hp(ht(165));
        baseDefenseSkill = 25;
        baseAttackSkill  = 24;

        exp = 25;

        loot(Gold.class, 0.5f);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(40, 45);
    }

    @Override
    public int dr() {
        return 2;
    }

    @Override
    public boolean canBePet() {
        return false;
    }
}
