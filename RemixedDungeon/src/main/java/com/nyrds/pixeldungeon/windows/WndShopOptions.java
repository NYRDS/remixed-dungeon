package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.BuyItemSelector;
import com.nyrds.pixeldungeon.utils.SellItemSelector;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndOptions;

public class WndShopOptions extends WndOptions {
    private final Char client;
    private final Bag backpack;

    private final Char shopkeeper;

    public WndShopOptions(Char shopkeeper, Char client) {
        super(Utils.capitalize(shopkeeper.getName()),
                StringsManager.getVar(R.string.Shopkeeper_text),
                StringsManager.getVar(R.string.Shopkeeper_SellPrompt),
                StringsManager.getVar(R.string.Shopkeeper_BuyPrompt));
        this.client = client;
        this.backpack = shopkeeper.getBelongings().backpack;
        this.shopkeeper = shopkeeper;
    }

    @Override
    public void onSelect(int index) {
        switch (index) {
            case 0:
                showSellWnd();
                break;
            case 1:
                showBuyWnd();
                break;
        }

    }

    public void showBuyWnd() {
        GameScene.show(
            new WndBag(shopkeeper.getBelongings(),
                        backpack,
                        new BuyItemSelector(shopkeeper),
                        WndBag.Mode.FOR_BUY,
                    StringsManager.getVar(R.string.Shopkeeper_Buy)));
    }

    public void showSellWnd() {
        GameScene.show(
                new WndBag(client.getBelongings(),
                            client.getBelongings().backpack,
                            new SellItemSelector(shopkeeper),
                            WndBag.Mode.FOR_SALE,
                        StringsManager.getVar(R.string.Shopkeeper_Sell)));
    }
}
