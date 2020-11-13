package com.watabou.pixeldungeon.actors.hero;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;

public class Interact extends CharAction {
    public Char chr;
    public Interact(Char chr) {
        this.chr = chr;
    }

    public boolean act(Char hero) {
        if (Dungeon.level.adjacent(hero.getPos(), chr.getPos())) {

            hero.readyAndIdle();
            hero.getSprite().turnTo(hero.getPos(), chr.getPos());
            if (!chr.interact(hero)) {
                new Attack(chr).act(hero);
            }
            return false;

        }

        if (Dungeon.level.fieldOfView[chr.getPos()] && hero.getCloser(chr.getPos())) {
            return true;
        }

        hero.readyAndIdle();
        return false;
    }
}
