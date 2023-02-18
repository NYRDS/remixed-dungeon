package com.watabou.pixeldungeon.scenes;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;

import org.jetbrains.annotations.NotNull;

class DefaultCellListener implements CellSelector.Listener {
    @Override
    public void onSelect(Integer cell, @NotNull Char selector) {
        if(cell!=null) {
            if (GameLoop.scene().cellClicked(cell)) { //handled by gameScene script
                return;
            }

            if(selector.level().cellClicked(cell)) { //handled by one level scripts
                return;
            }

            selector.handle(cell);
        }
    }

    @Override
    public String prompt() {
        return null;
    }

    @Override
    public Image icon() {
        return null;
    }
}
