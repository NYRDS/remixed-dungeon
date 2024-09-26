package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class Hunting extends MobAi implements AiState {

    public Hunting() { }

    @Override
    public void act(@NotNull Char me) {

        if(returnToOwnerIfTooFar(me, 6)) {
            return;
        }

        final Char enemy = me.getEnemy();

        if(enemy.invalid()) {
            me.setEnemy(chooseEnemy(me,1.0f));
        }

        if(me.friendly(enemy)) {
            me.setState(getStateByClass(Wandering.class));
            return;
        }

        me.enemySeen = me.isEnemyInFov();

        if (me.enemySeen && me.canAttack(enemy)) {
            me.doAttack(enemy);
        } else {
            if (me.enemySeen) {
                me.setTarget(enemy.getPos());
            }

            if(!me.doStepTo(me.getTarget())) {
                me.setTarget(me.level().randomDestination());
                me.setState(getStateByClass(Wandering.class));
            }
        }
    }

    @Override
    public String status(Char me) {
        if (me.getEnemy().valid()) {
            return Utils.format(R.string.Mob_StaHuntingStatus2,
                    me.getName(), me.getEnemy().getName_objective());
        }
        return Utils.format(R.string.Mob_StaHuntingStatus,
                me.getName());
    }

    @Override
    public void gotDamage(Char me, NamedEntityKind src, int dmg) {
        if(!me.isEnemyInFov()) {
            seekRevenge(me,src);
        }
    }

}
