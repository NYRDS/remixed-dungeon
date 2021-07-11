package com.nyrds.pixeldungeon.utils;

import com.nyrds.LuaInterface;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.android.util.Util;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.DummyChar;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CharsList {

    // Unreachable target
    public static final Char DUMMY = new DummyChar();

    private static final ConcurrentHashMap<Integer, Char> charsMap = new ConcurrentHashMap<>();

    static public List<Mob> emptyMobList = Collections.unmodifiableList(new ArrayList<>());

    @LuaInterface
    @NotNull
    static public Char getById(int id) {
        Char ret = charsMap.get(id);
        if(ret == null) {
            return DUMMY;
        }
        return ret;
    }

    static public boolean add(Char mob, int id) {
        if(charsMap.containsKey(id)) {
            GLog.debug("%s is duplicate, %s already here with id %d", mob.getEntityKind(), charsMap.get(id).getEntityKind(), id);
            if(Util.isDebug() && mob instanceof Hero) {
                throw new TrackedRuntimeException("dubbed Hero");
            }
            return false;
        }
        charsMap.put(id,mob);
        return true;
    }

    static public void remove(int id) {
        charsMap.remove(id);
    }

    static public void reset() {
        charsMap.clear();
    }
}
