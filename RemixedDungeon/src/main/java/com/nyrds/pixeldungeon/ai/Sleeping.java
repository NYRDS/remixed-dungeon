package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Challenges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class Sleeping implements AiState {

    public static final String TAG = "SLEEPING";

    private Mob mob;

    public Sleeping(Mob mob){
        this.mob = mob;
    }

    @Override
    public boolean act(boolean enemyInFOV, boolean justAlerted) {
        if (enemyInFOV
                && Random.Int(mob.distance(mob.getEnemy()) + mob.getEnemy().stealth()
                + (mob.getEnemy().isFlying() ? 2 : 0)) == 0) {

            mob.enemySeen = true;

            mob.notice();
            mob.setState(mob.HUNTING);
            mob.target = mob.getEnemy().getPos();

            if (Dungeon.isChallenged(Challenges.SWARM_INTELLIGENCE)) {
                for (Mob mob : Dungeon.level.mobs) {
                    if (this.mob != mob) {
                        mob.beckon(mob.target);
                    }
                }
            }

            mob.spend(Mob.TIME_TO_WAKE_UP);

        } else {

            mob.enemySeen = false;

            mob.spend(Actor.TICK);

        }
        return true;
    }

    @Override
    public String status() {
        return Utils.format(Game.getVar(R.string.Mob_StaSleepingStatus),
                mob.getName());
    }
}
