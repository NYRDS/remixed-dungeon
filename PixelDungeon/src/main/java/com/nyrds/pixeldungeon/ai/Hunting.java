package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

public class Hunting extends MobAi implements AiState {

    public Hunting() { }

    @Override
    public boolean act(Mob me) {
        me.enemySeen = me.isEnemyInFov();

        if (me.enemySeen && me.canAttack(me.getEnemy())) {
            return me.doAttack(me.getEnemy());
        } else {
            if (me.enemySeen) {
                me.target = me.getEnemy().getPos();
            }

            if(!me.doStepTo(me.target)) {
                me.spend(Actor.TICK);
                me.target = Dungeon.level.randomDestination();

                me.setState(getStateByClass(Wandering.class));
            }
        }
        return true;
    }

    @Override
    public String status(Mob me) {
        if (me.getEnemy().valid()) {
            return Utils.format(Game.getVar(R.string.Mob_StaHuntingStatus2),
                    me.getName(), me.getEnemy().getName_objective());
        }
        return Utils.format(Game.getVar(R.string.Mob_StaHuntingStatus),
                me.getName());
    }

    @Override
    public void gotDamage(Mob me, Object src, int dmg) {
        if(!me.isEnemyInFov()) {
            seekRevenge(me,src);
        }
    }

}
