package com.nyrds.pixeldungeon.mobs.guts;

import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 19.04.2016
 */
public class SpiritOfPain extends Mob {

    {
        hp(ht(80));
        baseDefenseSkill = 30;
        baseAttackSkill  = 30;

        exp = 0;

        setState(MobAi.getStateByClass(Hunting.class));
        flying = true;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(5, 10);
    }

    @Override
    public int dr() {
        return 20;
    }

    @Override
    public boolean act(){
        super.act();
        this.damage(6, this);
        return true;
    }

}