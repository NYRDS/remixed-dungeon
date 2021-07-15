package com.nyrds.pixeldungeon.ml.actions;

import com.watabou.pixeldungeon.actors.Char;

public class Move extends CharAction {
    public Move(int dst ) {
        this.dst = dst;
    }

    public boolean act(Char hero) {
        if (hero.getCloser(dst)) {
            return true;
        } else {
            hero.readyAndIdle();
            return false;
        }
    }
}
