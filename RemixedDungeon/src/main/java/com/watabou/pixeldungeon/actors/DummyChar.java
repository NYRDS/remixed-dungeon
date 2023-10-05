package com.watabou.pixeldungeon.actors;

import com.nyrds.pixeldungeon.ml.actions.CharAction;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.BuffCallback;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.DummySprite;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class DummyChar extends Char {

    public DummyChar() {
        super();
    }

    @Override
    protected float _attackDelay() {
        return 1f;
    }

    @Override
    public CharSprite newSprite() {return DummySprite.instance;}

    @Override
    public CharSprite getSprite() {return DummySprite.instance;}

    @Override
    protected void moveSprite(int oldPos, int pos) {
    }

    @Override
    public boolean getCloser(int cell) {
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
    public void resurrect() { }

    @Override
    public boolean dontPack() {
        return true;
    }

    @Override
    public int getPos() {
        return Level.INVALID_CELL;
    }

    @Override
    public void setPos(int pos) {
    }

    public boolean add(Buff buff) {
        GLog.debug("%s (%s) added to %s", buff.getEntityKind(), buff.getSource().getEntityKind(), getEntityKind());
        return false;
    }

    public void remove(@Nullable Buff buff) {
        if(buff!=null) {
            GLog.debug("%s removed from %s", buff.getEntityKind(), getEntityKind());
        }
    }

    @Override
    public boolean hasBuff(Class<? extends Buff> c) {
        return false;
    }

    @Override
    public int buffLevel(String buffName) {
        return 0;
    }

    @Override
    public <T extends Buff> HashSet<T> buffs(Class<T> c) {
        return new HashSet<>();
    }


    @Override
    public Buff buff(String buffName) {
        return null;
    }

    @Override
    public <T extends Buff> T buff(Class<T> c) {
        return null;
    }

    @Override
    public void forEachBuff(BuffCallback cb) {
    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public CharAction getCurAction() {
        return null;
    }
    @Override
    public void setCurAction(CharAction curAction) {
    }
}
