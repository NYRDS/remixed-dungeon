package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class Wandering implements AiState {

    public static final String TAG = "WANDERING";

    private Mob mob;

    public Wandering(Mob mob){
        this.mob = mob;
    }

    @Override
    public boolean act(boolean enemyInFOV, boolean justAlerted) {
        if (enemyInFOV
                && (justAlerted || Random.Int(mob.distance(mob.getEnemy()) / 2
                + mob.getEnemy().stealth()) == 0)) {

            mob.enemySeen = true;

            mob.notice();
            mob.setState(mob.HUNTING);
            mob.target = mob.getEnemy().getPos();

        } else {

            mob.enemySeen = false;

            int oldPos = mob.getPos();
            if (Dungeon.level.cellValid(mob.target) && mob.getCloser(mob.target)) {
                mob.spend(1 / mob.speed());
                return mob.moveSprite(oldPos, mob.getPos());
            } else {
                mob.target = Dungeon.level.randomDestination();
                mob.spend(Actor.TICK);
            }

        }
        return true;
    }

    @Override
    public String status() {
        return Utils.format(Game.getVar(R.string.Mob_StaWanderingStatus),
                mob.getName());
    }
}
