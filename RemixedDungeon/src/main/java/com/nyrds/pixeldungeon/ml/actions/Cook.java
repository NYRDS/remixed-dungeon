package com.nyrds.pixeldungeon.ml.actions;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.levels.features.AlchemyPot;

public class Cook extends CharAction {
    public Cook(int pot ) {
        this.dst = pot;
    }

    @Override
    public boolean act(Char hero) {

        if (Dungeon.isCellVisible(dst)) {
            hero.readyAndIdle();
            AlchemyPot.operate(hero, dst);
            return false;
        } else if (hero.getCloser(dst)) {
            return true;
        } else {
            hero.readyAndIdle();
            return false;
        }
    }
}
