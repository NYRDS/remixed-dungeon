
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.ItemOwner;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.rings.RingOfHaggler;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class WndTradeItem extends Window {

    private static final int WIDTH = 120;
    private static final int BTN_HEIGHT = 18;

    private final VBox vbox = new VBox();

    @NotNull
    private final ItemOwner shopkeeper;
    private final Hero customer;

    private static final int[] tradeQuantity = {1, 5, 10, 50, 100, 500, 1000};

    public WndTradeItem(final Item item, @NotNull ItemOwner shopkeeper, boolean buy) {

        super();

        this.shopkeeper = shopkeeper;
        this.customer = Dungeon.hero;

        add(vbox);

        if (buy) {
            makeBuyWnd(item);
        } else {
            makeSellWnd(item);
        }
    }

    private void makeSellWnd(final Item item) {
        float pos = createDescription(item, false);

        vbox.clear();
        int priceAll = price(item, false);

        if (item.quantity() == 1) {

            RedButton btn = new RedButton(Utils.format(R.string.WndTradeItem_Sell, priceAll)) {
                @Override
                protected void onClick() {
                    sell(item, 1);
                }
            };
            btn.setSize(WIDTH, BTN_HEIGHT);
            vbox.add(btn);

        } else {

            for (int i = 0; i < tradeQuantity.length; ++i) {
                if (item.quantity() > tradeQuantity[i]) {
                    final int finalI = i;
                    final int priceFor = priceAll / item.quantity() * tradeQuantity[i];
                    RedButton btnSellN = new RedButton(Utils.format(R.string.WndTradeItem_SellN,
                            tradeQuantity[finalI],
                            priceFor)) {
                        @Override
                        protected void onClick() {
                            sell(item, tradeQuantity[finalI]);
                        }
                    };
                    btnSellN.setSize(WIDTH, BTN_HEIGHT);
                    vbox.add(btnSellN);
                }
            }

            RedButton btnSellAll = new RedButton(Utils.format(R.string.WndTradeItem_SellAll, priceAll)) {
                @Override
                protected void onClick() {
                    sell(item, item.quantity());
                }
            };
            btnSellAll.setSize(WIDTH, BTN_HEIGHT);
            vbox.add(btnSellAll);
        }

        RedButton btnCancel = new RedButton(R.string.WndTradeItem_Cancel) {
            @Override
            protected void onClick() {
                hide();
            }
        };
        btnCancel.setSize(WIDTH, BTN_HEIGHT);
        vbox.add(btnCancel);

        vbox.setPos(0, pos + GAP);

        resize(WIDTH, (int) vbox.bottom());
    }

    private void makeBuyWnd(final Item item) {
        float pos = createDescription(item, true);

        vbox.clear();

        int priceAll = price(item, true);

        if (item.quantity() == 1) {

            RedButton btnBuy = new RedButton(Utils.format(R.string.WndTradeItem_Buy, priceAll)) {
                @Override
                protected void onClick() {
                    buy(item, 1);
                }
            };
            btnBuy.setSize(WIDTH, BTN_HEIGHT);
            btnBuy.enable(priceAll <= customer.gold());
            vbox.add(btnBuy);

        } else {
            for (int i = 0; i < tradeQuantity.length; ++i) {
                if (item.quantity() > tradeQuantity[i]) {
                    final int priceFor = priceAll / item.quantity() * tradeQuantity[i];
                    final int finalI = i;
                    RedButton btnBuyN = new RedButton(Utils.format(R.string.WndTradeItem_BuyN,
                            tradeQuantity[finalI],
                            priceFor)) {
                        @Override
                        protected void onClick() {
                            buy(item, tradeQuantity[finalI]);
                        }
                    };
                    btnBuyN.enable(priceFor <= customer.gold());
                    btnBuyN.setSize(WIDTH, BTN_HEIGHT);
                    vbox.add(btnBuyN);
                }
            }

            RedButton btnBuyAll = new RedButton(Utils.format(R.string.WndTradeItem_BuyAll, priceAll)) {
                @Override
                protected void onClick() {
                    buy(item, item.quantity());
                }
            };

            btnBuyAll.setSize(WIDTH, BTN_HEIGHT);
            btnBuyAll.enable(priceAll <= customer.gold());
            vbox.add(btnBuyAll);
        }
        RedButton btnCancel = new RedButton(R.string.WndTradeItem_Cancel) {
            @Override
            protected void onClick() {
                hide();
            }
        };
        btnCancel.setSize(WIDTH, BTN_HEIGHT);
        vbox.add(btnCancel);
        vbox.setPos(0, pos + GAP);

        resize(WIDTH, (int) vbox.bottom());
    }

    private float createDescription(Item item, boolean buying) {

        // Title
        IconTitle titlebar = new IconTitle();
        titlebar.icon(new ItemSprite(item));
        titlebar.label(buying ?
                Utils.format(R.string.WndTradeItem_Sale, item.toString(), price(item, true)) :
                Utils.capitalize(item.toString()));
        titlebar.setRect(0, 0, WIDTH, 0);
        add(titlebar);

        // Upgraded / degraded
        if (item.isLevelKnown() && item.level() > 0) {
            titlebar.color(ItemSlot.UPGRADED);
        } else if (item.isLevelKnown() && item.level() < 0) {
            titlebar.color(ItemSlot.DEGRADED);
        }

        // Description
        Text info = PixelScene.createMultiline(item.info(), GuiProperties.regularFontSize());
        info.maxWidth(WIDTH);
        info.setX(titlebar.left());
        info.setY(titlebar.bottom() + GAP);
        add(info);

        return info.getY() + info.height();
    }

    private int price(@NotNull Item item, boolean buying) {
        if (buying) {
            int price = shopkeeper.priceSell(item);
            if (Dungeon.hero.hasBuff(RingOfHaggler.Haggling.class) && price >= 2) {
                price /= 2;
            }
            return price;
        }

        int price = shopkeeper.priceBuy(item);
        if (Dungeon.hero.hasBuff(RingOfHaggler.Haggling.class)) {
            price *= 1.5f;
        }
        return price;
    }

    private void buy(@NotNull Item item, final int quantity) {
        Item boughtItem = item.detach(shopkeeper.getBelongings().backpack, quantity);

        int price = price(boughtItem, true);
        customer.spendGold(price);

        GLog.i(StringsManager.getVar(R.string.WndTradeItem_Bought), boughtItem.name(), price);

        if (!boughtItem.doPickUp(customer)) {
            boughtItem.doDrop(customer);
        }

        if (boughtItem != item) {
            hide();
            GameScene.show(new WndTradeItem(item, shopkeeper, true));
        } else {
            shopkeeper.generateNewItem();
            hide();
        }
    }

    private void sell(Item item, final int quantity) {

        if (item.isEquipped(customer) && !((EquipableItem) item).doUnequip(customer, false)) {
            hide();
            return;
        }

        Item soldItem = item.detach(customer.getBelongings().backpack, quantity);
        //shopkeeper.placeItemInShop(soldItem);

        int price = price(soldItem, false);

        new Gold(price).doPickUp(customer);
        hide();
        GLog.i(StringsManager.getVar(R.string.WndTradeItem_Sold), soldItem.name(), price);

        if (soldItem != item) {
            GameScene.show(new WndTradeItem(item, shopkeeper, false));
        }

    }

    @Override
    public void hide() {
        super.hide();
        if (WndBag.getInstance() != null) {
            WndBag.getInstance().updateItems();
        }
    }
}
