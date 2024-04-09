package com.nyrds.pixeldungeon.ml.actions;

import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;

public class InteractObject extends CharAction {
    public final LevelObject obj;
    public InteractObject(LevelObject obj) {
        this.obj = obj;
        dst = obj.getPos();
    }

    public boolean act(Char hero) {

        if (hero.adjacent(obj)) {

            hero.readyAndIdle();
            hero.getSprite().turnTo(hero.getPos(), dst);
            obj.interact(hero);
            return false;

        }

        if (Dungeon.level.fieldOfView[obj.getPos()] && hero.getCloser(dst)) {
            return true;
        }

        hero.readyAndIdle();
        return false;
    }
}
