package com.watabou.pixeldungeon.windows;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.PseudoItem;

import org.jetbrains.annotations.NotNull;

class ItemPlaceholder extends PseudoItem {

    public static final int RIGHT_HAND = 0;
    public static final int BODY = 1;
    public static final int LEFT_HAND = 2;
    public static final int ARTIFACT = 3;
    public static final int HELMET   = 5;
    public static final int LOCKED   = 6;

    public ItemPlaceholder(int image ) {
        this.imageFile = "items/placeholders.png";
        this.image     = image;
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
