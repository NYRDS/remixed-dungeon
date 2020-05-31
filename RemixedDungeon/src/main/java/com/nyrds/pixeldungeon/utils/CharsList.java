package com.nyrds.pixeldungeon.utils;

import com.nyrds.pixeldungeon.items.DummyItem;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.DummyChar;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.EquipableItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CharsList {

    // Unreachable target
    public static final Char DUMMY = new DummyChar();
    public static final Belongings EMPTY_BELONGINGS = new Belongings(DUMMY);
    public static final EquipableItem DUMMY_ITEM = new DummyItem();

    private static ConcurrentHashMap<Integer, Char> charsMap = new ConcurrentHashMap<>();

    static public List<Mob> emptyMobList = Collections.unmodifiableList(new ArrayList<>());

    @NotNull
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
