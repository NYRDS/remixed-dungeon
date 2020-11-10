package com.watabou.pixeldungeon.actors.hero;

public class Move extends CharAction {
    public Move(int dst ) {
        this.dst = dst;
    }

    public boolean act(Hero hero) {
        if (hero.getCloser(dst)) {
            return true;
        } else {
            hero.readyAndIdle();
            return false;
        }
    }
}
