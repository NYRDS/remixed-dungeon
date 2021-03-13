package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.levels.cellCondition;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.levels.Level;

public class BlinkAwayFromChar implements cellCondition {
    private final Char enemy;
    private final int dist;

    public BlinkAwayFromChar(Char enemy, int dist) {
        this.enemy = enemy;
        this.dist = dist;
    }

    @Override
    public boolean pass(Level level, int cell) {
        return level.distance(cell, enemy.getPos()) > dist && (level.passable[cell] || level.avoid[cell]) && Actor.findChar(cell) == null;
    }
}
