package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.utils.HUtils;

import org.jetbrains.annotations.NotNull;

public class KillOrder extends MobAi implements AiState {

    public KillOrder() { }

    @Override
    public void act(@NotNull Char me) {
        final Char enemy = me.getEnemy();

        if(enemy.invalid() || !enemy.isAlive()) {
            me.setState(getStateByClass(Hunting.class));
        }

        me.enemySeen = true;

        if (me.canAttack(enemy)) {
            me.doAttack(enemy);
        } else {
            if (me.enemySeen) {
                me.setTarget(enemy.getPos());
            }

            if(!me.doStepTo(me.getTarget())) {
                me.setState(getStateByClass(Hunting.class));
            }
        }
    }

    @Override
    public String status(Char me) {
        if (me.getEnemy().valid()) {
            return HUtils.format(R.string.Mob_StaHuntingStatus2,
                    me.getName(), me.getEnemy().getName_objective());
        }
        return HUtils.format(R.string.Mob_StaHuntingStatus,
                me.getName());
    }

    @Override
    public void gotDamage(Char me, NamedEntityKind src, int dmg) {
        if(!me.isEnemyInFov()) {
            seekRevenge(me,src);
        }
    }

}
