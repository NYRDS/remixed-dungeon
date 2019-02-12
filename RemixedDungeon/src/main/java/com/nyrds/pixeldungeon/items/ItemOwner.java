package com.nyrds.pixeldungeon.items;

import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.items.Item;

public interface ItemOwner {
    Belongings getBelongings();
    void collect(Item item);
}
