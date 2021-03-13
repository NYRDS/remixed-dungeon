package com.nyrds.pixeldungeon.items;

import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.items.Item;

import org.jetbrains.annotations.NotNull;

public interface ItemOwner {
    @NotNull
    Belongings getBelongings();
    boolean collect(Item item);
    boolean useBags();

    void generateNewItem();

    int priceSell(Item item);
    int priceBuy(Item item);
}
