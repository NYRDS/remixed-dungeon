package com.nyrds.pixeldungeon.utils;

import com.watabou.pixeldungeon.actors.Char;

import java.util.concurrent.ConcurrentHashMap;

public class CharsList {

    static ConcurrentHashMap<Integer, Char> charsMap = new ConcurrentHashMap<>();

    static public Char getById(int id) {
        return charsMap.get(id);
    }

    static public void add(Char mob, int id) {
        charsMap.put(id,mob);
    }

    static public void remove(int id) {
        charsMap.remove(id);
    }


}
