package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class Sleeping extends MobAi implements AiState {

    public Sleeping(){}

    @Override
    public void act(Mob me) {

        if(returnToOwnerIfTooFar(me, 3)) {
            return;
        }

        Char enemy = chooseEnemy(me);
        me.setEnemy(enemy);

        if (me.isEnemyInFov()
                && Random.Int(me.distance(enemy) + enemy.stealth()
                + (enemy.isFlying() ? 2 : 0)) == 0) {

            huntEnemy(me);

            me.spend(Mob.TIME_TO_WAKE_UP);

        } else {

            me.enemySeen = false;
            me.spend(Actor.TICK);

        }
    }

    @Override
    public void gotDamage(Mob me,Object src, int dmg) {
        seekRevenge(me,src);
    }

    @Override
    public String status(Mob me) {
        return Utils.format(Game.getVar(R.string.Mob_StaSleepingStatus),
                me.getName());
    }
}
