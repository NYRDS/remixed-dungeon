package com.watabou.pixeldungeon.scenes;

import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Char;

import org.jetbrains.annotations.NotNull;

class DefaultCellListener implements CellSelector.Listener {
    @Override
    public void onSelect(Integer cell, @NotNull Char selector) {
        if(cell!=null) {
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
