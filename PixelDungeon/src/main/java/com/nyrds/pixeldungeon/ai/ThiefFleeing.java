package com.nyrds.pixeldungeon.ai;

import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.sprites.CharSprite;

public class ThiefFleeing extends Fleeing {
    public ThiefFleeing(Mob mob) {
        super(mob);
    }

    @Override
    protected void nowhereToRun() {
        if (mob.hasBuff( Terror.class )) {
            super.nowhereToRun();
        } else {
            mob.getSprite().showStatus( CharSprite.NEGATIVE, Mob.TXT_RAGE );
            mob.setState(mob.HUNTING);
        }
    }
}
