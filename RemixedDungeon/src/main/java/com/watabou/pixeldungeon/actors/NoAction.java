package com.watabou.pixeldungeon.actors;

import com.nyrds.pixeldungeon.ml.actions.CharAction;

public class NoAction extends CharAction {
    @Override
    public void act(Char hero) {
    }

    @Override
    public boolean valid() {
        return false;
    }
}
