package com.watabou.pixeldungeon;

import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.levels.Level;

/**
 * Created by mike on 15.02.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class ClassicDungeonTilemap extends DungeonTilemap {

    public ClassicDungeonTilemap(Level level, String tiles) {
        super(level, tiles);
        map(level.map, level.getWidth());
    }

    @Override
    public Image tile(int pos) {
        Image img = new Image(getTexture());
        img.frame(getTileset().get(level.map[pos]));
        return img;
    }

    public void updateAll() {
        updated.set(0, 0, level.getWidth(), level.getHeight());
    }

    public void updateCell(int cell, Level level) {
        int x = level.cellX(cell);
        int y = level.cellY(cell);
        updated.union(x, y);
    }

}
