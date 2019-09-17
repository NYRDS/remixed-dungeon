package com.watabou.pixeldungeon.windows;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;

import org.jetbrains.annotations.NotNull;

class ItemPlaceholder extends Item {

    public ItemPlaceholder(int image ) {
        this.image = image;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public boolean isEquipped(@NotNull Char chr ) {
        return true;
    }
}
