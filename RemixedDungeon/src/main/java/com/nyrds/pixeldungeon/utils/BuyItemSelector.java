package com.nyrds.pixeldungeon.utils;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndTradeItem;

public class BuyItemSelector implements WndBag.Listener {
    private final Char shopkeeper;

    public BuyItemSelector(Char shopkeeper) {
        this.shopkeeper = shopkeeper;
    }

    @Override
    public void onSelect(Item item, Char selector) {
        if (item != null) {
            GameScene.show(new WndTradeItem(item, shopkeeper, true));
        }
    }
}
