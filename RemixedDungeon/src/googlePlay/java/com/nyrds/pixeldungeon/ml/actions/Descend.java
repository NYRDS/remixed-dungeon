package com.nyrds.pixeldungeon.ml.actions;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.scenes.InterlevelScene;

public class Descend extends CharAction {
    public Descend(int stairs ) {
        this.dst = stairs;
    }

    @Override
    public boolean act(Char hero) {

        if (hero.getPos() == dst && Dungeon.level.isExit(hero.getPos())) {
            Dungeon.level.onHeroDescend(hero.getPos());
            hero.clearActions();
            if (!Dungeon.level.isSafe()) {
                hero.hunger().satisfy(-Hunger.STARVING / 10);
            }
            InterlevelScene.Do(InterlevelScene.Mode.DESCEND);
            return false;
        }

        if (hero.getCloser(dst)) {
            return true;
        }

        hero.readyAndIdle();
        return false;
    }
}
