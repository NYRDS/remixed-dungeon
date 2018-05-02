package com.watabou.pixeldungeon.ui;

import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndCatalogus;
import com.watabou.pixeldungeon.windows.elements.Tool;

/**
 * Created by mike on 01.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
class InventoryTool extends Tool {
    private GoldIndicator gold;
    private PickedUpItem  pickedUpItem;

    public InventoryTool() {
        super(Assets.UI_ICONS, 16);
    }

    @Override
    protected void onClick() {
        GameScene.show(new WndBag(Dungeon.hero.belongings.backpack,
                null, WndBag.Mode.ALL, null));
    }

    protected boolean onLongClick() {
        GameScene.show(new WndCatalogus());
        return true;
    }

    @Override
    protected void createChildren() {
        super.createChildren();
        gold = new GoldIndicator();
        gold.setPos(x,y);
        add(gold);

        pickedUpItem = new PickedUpItem();
        pickedUpItem.setPos(x,y);
        add(pickedUpItem);
    }

    @Override
    protected void layout() {
        super.layout();
        gold.fill(this);
    }

    public void pickUp(Item item) {
        pickedUpItem.reset(item,x,y);
    }
}
