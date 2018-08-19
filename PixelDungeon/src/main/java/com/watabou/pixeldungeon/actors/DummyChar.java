package com.watabou.pixeldungeon.actors;

import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.sprites.CharSprite;

class DummyChar extends Char {

    {
        setPos(Level.INVALID_CELL);
    }

    @Override
    protected CharSprite sprite() {
        return null;
    }

    @Override
    protected void moveSprite(int oldPos, int pos) {}

    @Override
    protected boolean getCloser(int cell) {
        return false;
    }

    @Override
    protected boolean getFurther(int cell) {
        return false;
    }

    @Override
    public boolean dontPack() {
        return true;
    }
}
