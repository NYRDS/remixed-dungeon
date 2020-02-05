package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

public class RunningAmok extends MobAi implements AiState {

    @Override
    public void act(Mob me) {

        if(!me.hasBuff(Amok.class)) {
            me.setState(MobAi.getStateByClass(Wandering.class));
            return;
        }

        me.setEnemy(chooseNearestChar(me));

        me.enemySeen = me.isEnemyInFov();

        if(me.enemySeen) {
            me.target = me.getEnemy().getPos();
        } else {
            me.target = me.level().randomDestination();
        }

        if (me.canAttack(me.getEnemy())) {
            me.doAttack(me.getEnemy());
        } else {
            me.doStepTo(me.target);
        }
    }

    @Override
    public void gotDamage(Mob me, NamedEntityKind src, int dmg) {
    }

    @Override
    public String status(Mob me) {
        return Utils.format(Game.getVar(R.string.Mob_StaAmokStatus),
                me.getName());
    }
}
