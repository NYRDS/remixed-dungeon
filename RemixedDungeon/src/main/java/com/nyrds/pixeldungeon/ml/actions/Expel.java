package com.nyrds.pixeldungeon.ml.actions;

import static com.watabou.pixeldungeon.actors.Actor.TICK;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.mobs.Fraction;

import org.jetbrains.annotations.NotNull;

public class Expel extends CharAction {
    private final Char target;

    public Expel(@NotNull Char target) {
        this.target = target;
        dst = target.getPos();
    }

    @Override
    public boolean act(Char hero) {
        hero.spend(TICK/10);
        hero.setCurAction(null);
        hero.busy();
        target.setOwnerId(target.getId());
        target.setFraction(Fraction.DUNGEON);
        CharUtils.clearMarkers();
        return false;
    }
}
