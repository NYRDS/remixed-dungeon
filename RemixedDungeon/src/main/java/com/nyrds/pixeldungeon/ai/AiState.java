package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;

import org.jetbrains.annotations.NotNull;

public interface AiState {
    void act(@NotNull Mob me);

    String status(Char me);

    String getTag();

    void gotDamage(Mob me, NamedEntityKind src, int dmg);
    void onDie(@NotNull Mob me);
}
