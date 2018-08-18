package com.nyrds.pixeldungeon.ai;

import com.watabou.pixeldungeon.actors.mobs.Mob;

public class Terror extends MobAi implements AiState{

    @Override
    public boolean act(Mob me) {
        return false;
    }

    @Override
    public void gotDamage(Mob me, Object src, int dmg) {

    }
}
