package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.sprites.ItemSprite;

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
        return StringsManager.getVar(R.string.Item_DirThrow);
    }

    @Override
    public Image icon() {
        return new ItemSprite(item);
    }
}
