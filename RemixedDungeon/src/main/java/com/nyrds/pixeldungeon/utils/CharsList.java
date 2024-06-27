package com.nyrds.pixeldungeon.utils;


import com.nyrds.LuaInterface;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.DummyChar;
import com.watabou.pixeldungeon.actors.DummyHero;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CharsList {

    // Unreachable target
    public static final Char DUMMY = new DummyChar();
    public static final Hero DUMMY_HERO = new DummyHero();

    private static ConcurrentHashMap<Integer, Char> charsMap;
    private static final HashSet<Integer> destroyedChars = new HashSet<>();
    private static final String GRAVEYARD = "graveyard";

    static public final List<Mob> emptyMobList = Collections.unmodifiableList(new ArrayList<>());

    @LuaInterface
    @NotNull
    static public Char getById(int id) {
        Char ret = getCharsMap().get(id);
        if(ret == null) {
            return DUMMY;
        }
        return ret;
    }

    static public boolean add(Char mob, int id) {
        if(getCharsMap().containsKey(id)) {
            GLog.debug("%s is duplicate, %s already here with id %d", mob.getEntityKind(), getCharsMap().get(id).getEntityKind(), id);
            if(Util.isDebug() && mob instanceof Hero) {
                throw new TrackedRuntimeException("dubbed Hero");
            }
            return false;
        }
        getCharsMap().put(id,mob);
        return true;
    }

    static public void remove(int id) {
        getCharsMap().remove(id);
    }

    static public void destroy(int id) {
        destroyedChars.add(id);
        remove(id);
    }

    static public boolean isDestroyed(int id) {
        return destroyedChars.contains(id);
    }


    static public void storeInBundle(Bundle bundle) {
        bundle.put(GRAVEYARD, destroyedChars.toArray(new Integer[0]));
    }

    static public void restoreFromBundle(Bundle bundle) {
        destroyedChars.clear();
        for (int i : bundle.getIntArray(GRAVEYARD)) {
            destroyedChars.add(i);
        }
    }

    static public void reset() {
        getCharsMap().clear();
    }

    public static ConcurrentHashMap<Integer, Char> getCharsMap() {
        if(charsMap == null) {
            charsMap = new ConcurrentHashMap<>();
        }
        return charsMap;
    }
}
