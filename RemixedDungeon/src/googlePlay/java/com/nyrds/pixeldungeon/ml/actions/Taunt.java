package com.nyrds.pixeldungeon.ml.actions;

import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;

public class Taunt extends CharAction {
    private final Char target;

    public Taunt(Char target) {
        this.target = target;
        dst = target.getPos();
    }

    @Override
    public boolean act(Char hero) {
        target.setState(MobAi.getStateByClass(Hunting.class));
        target.setTarget(hero.getPos());
        hero.spend(Actor.TICK/10);;

        return false;
    }
}
