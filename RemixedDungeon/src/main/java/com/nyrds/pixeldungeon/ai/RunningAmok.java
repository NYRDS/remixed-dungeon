package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class RunningAmok extends MobAi implements AiState {

    @Override
    public void act(@NotNull Mob me) {

        if(!me.hasBuff(Amok.class)) {
            me.setState(MobAi.getStateByClass(Wandering.class));
            return;
        }

        me.setEnemy(chooseNearestChar(me));

        me.enemySeen = me.isEnemyInFov();

        if(me.enemySeen) {
            me.setTarget(me.getEnemy().getPos());
        } else {
            me.setTarget(me.level().randomDestination());
        }

        if (me.canAttack(me.getEnemy())) {
            me.doAttack(me.getEnemy());
        } else {
            me.doStepTo(me.getTarget());
        }
    }

    @Override
    public void gotDamage(Mob me, NamedEntityKind src, int dmg) {
    }

    @Override
    public String status(Char me) {
        return Utils.format(R.string.Mob_StaAmokStatus,
                me.getName());
    }
}
