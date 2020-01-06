package com.watabou.pixeldungeon.actors;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.pixeldungeon.Dungeon;

import org.jetbrains.annotations.Nullable;

public class CharUtils {
    static public boolean isVisible(@Nullable Char ch) {
        if(ch==null) {
            return false;
        }

        if(!ch.level().cellValid(ch.getPos())) {
            EventCollector.logException("Checking visibility on invalid cell");
            return false;
        }

        return Dungeon.visible[ch.getPos()];
    }
}
