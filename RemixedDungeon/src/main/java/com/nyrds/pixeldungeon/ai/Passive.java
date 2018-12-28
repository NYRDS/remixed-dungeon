package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

public class Passive implements AiState {

    public static final String TAG = "PASSIVE";

    private Mob mob;

    public Passive(Mob mob){
        this.mob = mob;
    }

    @Override
    public boolean act(boolean enemyInFOV, boolean justAlerted) {
        mob.enemySeen = false;
        mob.spend(Actor.TICK);
        return true;
    }

    @Override
    public String status() {
        return Utils.format(Game.getVar(R.string.Mob_StaPassiveStatus),
                mob.getName());
    }
}
