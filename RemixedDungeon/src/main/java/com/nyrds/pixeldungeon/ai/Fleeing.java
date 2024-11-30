package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class Fleeing extends MobAi implements AiState {


    public Fleeing(){}

    @Override
    public void act(@NotNull Char me) {
        me.enemySeen = me.isEnemyInFov();
        if (me.enemySeen) {
            me.setTarget(me.getEnemy().getPos());
        }

        me.doStepFrom(me.getTarget());
    }

    @Override
    public String status(Char me) {
        Char enemy = me.getEnemy();
        if(enemy.valid()) {
            return Utils.format(R.string.Mob_StaFleeingStatus2,
                    me.getName(), enemy.getName_objective());
        }
        return Utils.format(R.string.Mob_StaFleeingStatus,
                me.getName());
    }

    @Override
    public void gotDamage(Char me, NamedEntityKind src, int dmg) {
        seekRevenge(me,src);
    }
}
