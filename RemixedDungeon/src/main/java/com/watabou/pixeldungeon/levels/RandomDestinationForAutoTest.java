package com.watabou.pixeldungeon.levels;

import com.nyrds.pixeldungeon.levels.cellCondition;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;

class RandomDestinationForAutoTest implements cellCondition {

    public RandomDestinationForAutoTest() {
    }

    @Override
    public boolean pass(Level level, int cell) {
        LevelObject lo = level.getTopLevelObject(cell);
        if (lo != null && lo.getLayer() >= 0) {
            return false;
        }

        if(!level.visited[cell] && (level.passable[cell] || level.secret[cell] || level.avoid[cell]) )  {
            for (int i = 0; i < Level.NEIGHBOURS8.length; ++i) {
                int adjCell = cell + Level.NEIGHBOURS8[i];
                if (level.cellValid(adjCell) && level.passable[adjCell] && level.mapped[adjCell]) {
                    return true;
                }
            }
        }
        return false;
    }
}
