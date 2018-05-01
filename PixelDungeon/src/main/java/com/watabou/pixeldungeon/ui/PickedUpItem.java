package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite;

/**
 * Created by mike on 01.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
class PickedUpItem extends ItemSprite {

    private static final float DISTANCE = DungeonTilemap.SIZE;
    private static final float DURATION = 0.2f;

    private float dstX;
    private float dstY;
    private float left;

    PickedUpItem() {
        super();

        originToCenter();

        active = setVisible(false);
    }

    public void reset(Item item, float dstX, float dstY) {
        view(item);

        active = setVisible(true);

        this.dstX = dstX - ItemSprite.SIZE / 2;
        this.dstY = dstY - ItemSprite.SIZE / 2;
        left = DURATION;

        x = this.dstX - DISTANCE;
        y = this.dstY - DISTANCE;
        alpha(1);
    }

    @Override
    public void update() {
        super.update();

        if ((left -= Game.elapsed) <= 0) {

            setVisible(active = false);

        } else {
            float p = left / DURATION;
            scale.set((float) Math.sqrt(p));
            float offset = DISTANCE * p;
            x = dstX - offset;
            y = dstY - offset;
        }
    }
}
