package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.watabou.pixeldungeon.actors.Char;

import org.jetbrains.annotations.NotNull;

public interface AiState {
    void act(@NotNull Char me);

    String status(Char me);

    String getTag();

    void gotDamage(Char me, NamedEntityKind src, int dmg);
    void onDie(@NotNull Char me);
}
