package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.BuyItemSelector;
import com.nyrds.pixeldungeon.utils.SellItemSelector;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndOptions;

public class WndShopkeeperOptions extends WndOptions {
    private final Char client;
    private final Bag backpack;

    private final Shopkeeper shopkeeper;
    private final WndBag.Listener buyItemSelector;
    private final WndBag.Listener sellItemSelector;

    public WndShopkeeperOptions(Shopkeeper shopkeeper, Char client, Bag backpack) {
        super(Utils.capitalize(shopkeeper.getName()), Game.getVar(R.string.Shopkeeper_text), Game.getVar(R.string.Shopkeeper_SellPrompt), Game.getVar(R.string.Shopkeeper_BuyPrompt));
        this.client = client;
        this.backpack = backpack;
        this.shopkeeper = shopkeeper;
        this.buyItemSelector = new BuyItemSelector(shopkeeper);
        this.sellItemSelector =  new SellItemSelector(shopkeeper);
    }

    @Override
    public void onSelect(int index) {
        WndBag wndBag = null;

        switch (index) {
            case 0:
                wndBag = new WndBag(client.getBelongings(), client.getBelongings().backpack, sellItemSelector,WndBag.Mode.FOR_SALE, Game.getVar(R.string.Shopkeeper_Sell));
                break;
            case 1:
                wndBag = new WndBag(shopkeeper.getBelongings(), backpack, buyItemSelector,WndBag.Mode.FOR_BUY, Game.getVar(R.string.Shopkeeper_Buy));
                break;
        }

        if(wndBag!=null) {
            GameScene.show(wndBag);
        }
    }
}
