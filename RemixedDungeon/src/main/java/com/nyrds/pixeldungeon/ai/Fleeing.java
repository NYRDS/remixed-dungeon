package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

public class Fleeing extends MobAi implements AiState {


    public Fleeing(){}

    @Override
    public void act(Mob me) {
        me.enemySeen = me.isEnemyInFov();
        if (me.enemySeen) {
            me.setTarget(me.getEnemy().getPos());
        }

        if(!me.doStepFrom(me.getTarget())) {
            me.spend(Actor.TICK);
        }
    }

    @Override
    public String status(Char me) {
        Char enemy = me.getEnemy();
        if(enemy.valid()) {
            return Utils.format(Game.getVar(R.string.Mob_StaFleeingStatus2),
                    me.getName(), enemy.getName_objective());
        }
        return Utils.format(Game.getVar(R.string.Mob_StaFleeingStatus),
                me.getName());
    }

    @Override
    public void gotDamage(Mob me, NamedEntityKind src, int dmg) {
        seekRevenge(me,src);
    }
}
