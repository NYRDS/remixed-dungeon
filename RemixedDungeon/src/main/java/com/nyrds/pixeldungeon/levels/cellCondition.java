package com.nyrds.pixeldungeon.levels;

import com.watabou.pixeldungeon.levels.Level;

public interface cellCondition {
    boolean pass(Level level, int cell);
}
