package com.watabou.pixeldungeon.ui;

import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
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
    public PickedUpItem pickedUpItem = new PickedUpItem();

    public InventoryTool() {
        super(Assets.UI_ICONS, 0);
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
        add(gold);
    }

    @Override
    protected void layout() {
        super.layout();
        gold.fill(this);
    }
}
