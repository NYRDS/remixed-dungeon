package com.nyrds.pixeldungeon.ai;

import com.watabou.pixeldungeon.actors.mobs.Mob;

public interface AiState {
    boolean act(Mob me);

    String status(Mob me);

    String getTag();

    void gotDamage(Mob me, Object src, int dmg);
}
