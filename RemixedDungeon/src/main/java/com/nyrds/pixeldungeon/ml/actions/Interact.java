package com.nyrds.pixeldungeon.ml.actions;

import static com.watabou.pixeldungeon.actors.Actor.TICK;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;

public class Interact extends CharAction {
    public final Char chr;
    public Interact(Char chr) {
        this.chr = chr;
        dst = chr.getPos();
    }

    public void act(Char hero) {
        if(!chr.isOnStage()) {
            return;
        }

        if (hero.adjacent(chr)) {

            hero.getSprite().turnTo(hero.getPos(), dst);
            if (!chr.interact(hero)) {
                new Attack(chr).act(hero);
            }
            hero.readyAndIdle();
            return;

        } else {
            if (chr.getOwnerId() == hero.getId()) {
                hero.spend(TICK/10);
                hero.setCurAction(null);
                hero.selectCell(new OrderCellSelector(chr));
                return;
            }
        }

        if (hero.level().fieldOfView[chr.getPos()] && hero.getCloser(chr.getPos())) {
            return;
        }

        hero.readyAndIdle();
    }
}
