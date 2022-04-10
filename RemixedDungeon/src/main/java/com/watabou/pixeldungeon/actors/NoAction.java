package com.watabou.pixeldungeon.actors;

import com.nyrds.pixeldungeon.ml.actions.CharAction;

public class NoAction extends CharAction {
    @Override
    public boolean act(Char hero) {
        return false;
    }

    @Override
    public boolean valid() {
        return false;
    }
}
