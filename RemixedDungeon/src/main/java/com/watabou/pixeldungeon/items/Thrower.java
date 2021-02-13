package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.CellSelector;

import org.jetbrains.annotations.NotNull;

class Thrower implements CellSelector.Listener {
    private final Item item;

    public Thrower(Item item) {
        this.item = item;
    }

    @Override
    public void onSelect(Integer target, @NotNull Char selector) {
        if (target != null) {
            item.cast(selector, target);
        }
    }

    @Override
    public String prompt() {
        return Game.getVar(R.string.Item_DirThrow);
    }

    @Override
    public Image icon() {
        return null;
    }
}
