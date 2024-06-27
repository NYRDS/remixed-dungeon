package com.nyrds.pixeldungeon.mobs.guts;

import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;

/**
 * Created by DeadDie on 12.02.2016
 */
public class PseudoRat extends Mob {
    {
        hp(ht(320));
        baseDefenseSkill = 30;
        baseAttackSkill  = 30;
        dmgMin = 40;
        dmgMax = 70;
        dr = 25;

        expForKill = 20;
        maxLvl = 35;

        loot(Gold.class, 0.8f);

        setState(MobAi.getStateByClass(Hunting.class));

        addImmunity(Paralysis.class);
    }

    @Override
    public int getKind() {
        return 1;
    }
}
