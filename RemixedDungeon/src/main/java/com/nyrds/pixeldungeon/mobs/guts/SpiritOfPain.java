package com.nyrds.pixeldungeon.mobs.guts;

import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.watabou.pixeldungeon.actors.mobs.Mob;

/**
 * Created by DeadDie on 19.04.2016
 */
public class SpiritOfPain extends Mob {

    {
        carcassChance = 0;
        hp(ht(80));
        baseDefenseSkill = 30;
        baseAttackSkill  = 30;
        dmgMin = 5;
        dmgMax = 10;
        dr = 20;

        expForKill = 0;

        setState(MobAi.getStateByClass(Hunting.class));
        flying = true;
    }

    @Override
    public boolean act(){
        super.act();
        this.damage(6, this);
        return true;
    }

}