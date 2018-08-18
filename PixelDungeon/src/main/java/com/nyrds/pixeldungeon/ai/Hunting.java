package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

public class Hunting implements AiState {

    public static final String TAG = "HUNTING";

    private Mob mob;

    public Hunting(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean act(boolean enemyInFOV, boolean justAlerted) {
        mob.enemySeen = enemyInFOV;
        if (enemyInFOV && mob.canAttack(mob.getEnemy())) {
            return mob.doAttack(mob.getEnemy());
        } else {
            if (enemyInFOV) {
                mob.target = mob.getEnemy().getPos();
            }
            int oldPos = mob.getPos();
            if (mob.target != -1 && mob.getCloser(mob.target)) {

                mob.spend(1 / mob.speed());
                return mob.moveSprite(oldPos, mob.getPos());

            } else {

                mob.spend(Actor.TICK);
                mob.setState(mob.WANDERING);
                mob.target = Dungeon.level.randomDestination();
                return true;
            }
        }
    }

    @Override
    public String status() {
        if (mob.getEnemy()!= Char.DUMMY) {
            return Utils.format(Game.getVar(R.string.Mob_StaHuntingStatus2),
                    mob.getName(), mob.getEnemy().getName_objective());
        }
        return Utils.format(Game.getVar(R.string.Mob_StaHuntingStatus),
                mob.getName());
    }
}
