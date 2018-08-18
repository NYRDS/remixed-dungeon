package com.nyrds.pixeldungeon.ai;

import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.sprites.CharSprite;

public class ThiefFleeing extends Fleeing {

    public ThiefFleeing() {
    }

    @Override
    protected void nowhereToRun(Mob me) {
        if (me.hasBuff( Terror.class )) {
            super.nowhereToRun(me);
        } else {
            me.getSprite().showStatus( CharSprite.NEGATIVE, Mob.TXT_RAGE );
            me.setState(MobAi.getStateByClass(Hunting.class));
        }
    }
}
