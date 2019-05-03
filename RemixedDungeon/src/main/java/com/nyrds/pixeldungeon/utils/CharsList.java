package com.nyrds.pixeldungeon.utils;

import androidx.annotation.NonNull;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.DummyChar;
import com.watabou.pixeldungeon.actors.mobs.Mob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CharsList {

    // Unreachable target
    public static final Char DUMMY = new DummyChar();

    private static ConcurrentHashMap<Integer, Char> charsMap = new ConcurrentHashMap<>();

    static public List<Mob> emptyMobList = Collections.unmodifiableList(new ArrayList<>());

    @NonNull
    static public Char getById(int id) {
        Char ret = charsMap.get(id);
        if(ret == null) {
            return DUMMY;
        }
        return ret;
    }

    static public void add(Char mob, int id) {
        charsMap.put(id,mob);
    }

    static public void remove(int id) {
        charsMap.remove(id);
    }
}
