package com.nyrds.pixeldungeon.ml.actions;

import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;

import org.jetbrains.annotations.NotNull;

public class Push extends CharAction {
    private final Char target;

    public Push(@NotNull Char target) {
        this.target = target;
        dst = target.getPos();
    }

    @Override
    public boolean act(Char hero) {
        target.push(hero);
        hero.spend(Actor.TICK);
        hero.readyAndIdle();
        return false;
    }
}
