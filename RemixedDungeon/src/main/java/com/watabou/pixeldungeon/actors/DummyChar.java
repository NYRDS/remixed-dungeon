package com.watabou.pixeldungeon.actors;

import com.nyrds.pixeldungeon.utils.CharsList;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.DummySprite;

public class DummyChar extends Char {

    public DummyChar() {
        super();
    }

    @Override
    protected float _attackDelay() {
        return 1f;
    }

    @Override
    public CharSprite newSprite() {
        return new DummySprite();
    }

    @Override
    protected void moveSprite(int oldPos, int pos) {
    }

    @Override
    protected boolean getCloser(int cell) {
        return false;
    }

    @Override
    protected boolean getFurther(int cell) {
        return false;
    }

    @Override
    public Char makeClone() {
        return CharsList.DUMMY;
    }

    @Override
    public boolean dontPack() {
        return true;
    }

    @Override
    public int getPos() {
        return Level.INVALID_CELL;
    }
}
