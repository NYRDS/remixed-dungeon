package com.nyrds.pixeldungeon.ml.actions;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.InterlevelScene;

public class Descend extends CharAction {
    public Descend(int stairs ) {
        this.dst = stairs;
    }

    @Override
    public boolean act(Char hero) {

        final Level level = Dungeon.level;


        int pos = hero.getPos();
        if ((pos == dst || level.adjacent(pos, dst)) && level.isExit(dst)) {
            hero.setPos(dst);
            level.onHeroDescend(dst);
            hero.clearActions();
            if (!level.isSafe()) {
                hero.hunger().satisfy(-Hunger.STARVING / 10);
            }
            InterlevelScene.Do(InterlevelScene.Mode.DESCEND);
            return false;
        }

        if (hero.getCloser(dst, true)) {
            return true;
        }

        hero.readyAndIdle();
        return false;
    }
}
