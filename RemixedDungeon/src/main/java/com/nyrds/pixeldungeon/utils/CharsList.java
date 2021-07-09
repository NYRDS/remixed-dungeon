package com.nyrds.pixeldungeon.utils;

import com.nyrds.LuaInterface;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.DummyChar;
import com.watabou.pixeldungeon.actors.mobs.Mob;

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
            return false;
        }
        charsMap.put(id,mob);
        return true;
    }

    static public void remove(int id) {
        charsMap.remove(id);
    }
}
