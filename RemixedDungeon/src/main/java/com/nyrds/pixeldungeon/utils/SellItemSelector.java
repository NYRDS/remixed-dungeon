package com.nyrds.pixeldungeon.utils;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndTradeItem;

public class SellItemSelector implements WndBag.Listener {
    private final Char shopkeeper;

    public SellItemSelector(Char shopkeeper) {
        this.shopkeeper = shopkeeper;
    }

    @Override
    public void onSelect(Item item, Char selector) {
        if (item != null) {

            if(item instanceof Bag && !((Bag)item).items.isEmpty()) {
                GameScene.selectItemFromBag(this, (Bag)item , WndBag.Mode.FOR_SALE, StringsManager.getVar(R.string.Shopkeeper_Sell));
                return;
            }

            GameScene.show( new WndTradeItem( item, shopkeeper, false) );
        }
    }
}
