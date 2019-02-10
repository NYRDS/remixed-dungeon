package com.watabou.pixeldungeon.windows;

import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;

class ItemPlaceholder extends Item {

    public ItemPlaceholder(int image ) {
        this.image = image;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public boolean isEquipped( Hero hero ) {
        return true;
    }
}
