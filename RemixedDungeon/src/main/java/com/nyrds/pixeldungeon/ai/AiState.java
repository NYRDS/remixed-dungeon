package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;

public interface AiState {
    void act(Mob me);

    String status(Char me);

    String getTag();

    void gotDamage(Mob me, NamedEntityKind src, int dmg);
    void onDie();
}
