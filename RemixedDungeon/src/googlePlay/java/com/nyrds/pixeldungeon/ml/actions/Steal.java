package com.nyrds.pixeldungeon.ml.actions;

import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;

public class Steal extends CharAction {
    private final Char target;

    public Steal(Char target) {
        this.target = target;
        dst = target.getPos();
    }

    @Override
    public boolean act(Char hero) {
        CharUtils.steal(hero, target);
        hero.spend(Actor.TICK);
        hero.readyAndIdle();
        return false;
    }
}
