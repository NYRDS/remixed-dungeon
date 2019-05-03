package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
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
            me.target = me.getEnemy().getPos();
        }

        if(!me.doStepFrom(me.target)) {
            me.spend(Actor.TICK);
        }
    }

    @Override
    public String status(Mob me) {
        Char enemy = me.getEnemy();
        if(enemy != CharsList.DUMMY) {
            return Utils.format(Game.getVar(R.string.Mob_StaFleeingStatus2),
                    me.getName(), enemy.getName_objective());
        }
        return Utils.format(Game.getVar(R.string.Mob_StaFleeingStatus),
                me.getName());
    }

    @Override
    public void gotDamage(Mob me,Object src, int dmg) {
        seekRevenge(me,src);
    }
}
