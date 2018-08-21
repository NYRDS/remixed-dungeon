package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

public class Fleeing implements AiState {

    public static final String TAG = "FLEEING";

    protected Mob mob;

    public Fleeing(Mob mob){
        this.mob = mob;
    }

    @Override
    public boolean act(boolean enemyInFOV, boolean justAlerted) {
        mob.enemySeen = enemyInFOV;
        if (enemyInFOV) {
            mob.target = mob.getEnemy().getPos();
        }

        int oldPos = mob.getPos();
        if (Dungeon.level.cellValid(mob.target) && mob.getFurther(mob.target)) {

            mob.spend(1 / mob.speed());
            return mob.moveSprite(oldPos, mob.getPos());

        } else {

            mob.spend(Actor.TICK);
            nowhereToRun();

            return true;
        }
    }

    protected void nowhereToRun() {
    }

    @Override
    public String status() {
        return Utils.format(Game.getVar(R.string.Mob_StaFleeingStatus),
                mob.getName());
    }
}
