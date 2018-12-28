package com.watabou.pixeldungeon.ui;

import com.watabou.pixeldungeon.Chrome;
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
        super(10, Chrome.Type.ACTION_BUTTON);
        gold = new GoldIndicator();
        gold.setPos(x,y);
        add(gold);

        pickedUpItem = new PickedUpItem();
        add(pickedUpItem);
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
    protected void layout() {
        super.layout();
        gold.fill(this);
    }

    public void pickUp(Item item) {
        pickedUpItem.reset(item,x,y);
    }
}
