package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

public class Fleeing extends MobAi implements AiState {


    public Fleeing(){ }

    @Override
    public boolean act(Mob me) {
        me.enemySeen = me.isEnemyInFov();
        if (me.enemySeen) {
            me.target = me.getEnemy().getPos();
        }

        int oldPos = me.getPos();
        if (Dungeon.level.cellValid(me.target) && me.getFurther(me.target)) {

            me.spend(1 / me.speed());
            return me.moveSprite(oldPos, me.getPos());

        } else {

            me.spend(Actor.TICK);
            nowhereToRun(me);

            return true;
        }
    }

    protected void nowhereToRun(Mob me) {
    }

    @Override
    public String status(Mob me) {
        Char enemy = me.getEnemy();
        if(enemy != Char.DUMMY) {
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
