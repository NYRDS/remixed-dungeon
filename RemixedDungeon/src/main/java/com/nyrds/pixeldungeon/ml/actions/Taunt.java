package com.nyrds.pixeldungeon.ml.actions;

import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
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
        target.setEnemy(hero);
        target.notice();

        Sample.INSTANCE.play(Assets.SND_MIMIC);

        hero.spend(Actor.MICRO_TICK);
        hero.readyAndIdle();

        return false;
    }
}
