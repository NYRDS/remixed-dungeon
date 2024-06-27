package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class Wandering extends MobAi implements AiState {

    public Wandering(){ }

    @Override
    public void act(@NotNull Char me) {

        if(returnToOwnerIfTooFar(me, 2)) {
            me.spend(Actor.MICRO_TICK);
            return;
        }

        Char enemy = chooseEnemy(me, 1f);
        me.setEnemy(enemy);

        if (me.isEnemyInFov()) {
            huntEnemy(me);
        } else {

            me.enemySeen = false;

            if(!me.doStepTo(me.getTarget())) {
                me.setTarget(me.level().randomDestination());
                me.spend(Actor.TICK);
            }
        }
    }

    @Override
    public String status(Char me) {
        return Utils.format(R.string.Mob_StaWanderingStatus,
                me.getName());
    }

    @Override
    public void gotDamage(Char me, NamedEntityKind src, int dmg) {
        seekRevenge(me,src);
    }
}
