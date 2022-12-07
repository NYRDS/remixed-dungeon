package com.nyrds.retrodungeon.mobs.guts;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 19.04.2016
 */
public class SpiritOfPain extends Mob {

    {
        hp(ht(80));
        defenseSkill = 30;

        exp = 0;

        setState(HUNTING);
        flying = true;
    }

    @Override
    public int attackSkill(Char target) {
        return 30;
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
    protected boolean act(){
        super.act();
        this.damage(6, this);
        return true;
    }

}