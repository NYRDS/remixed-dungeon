package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite;

/**
 * Created by mike on 01.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
class PickedUpItem extends ItemSprite {

    private static final float DURATION = 0.2f;

    private float left;

    PickedUpItem() {
        super();

        originToCenter();

        active = setVisible(false);
    }

    public void reset(Item item, float dstX, float dstY) {
        view(item);

        active = setVisible(true);

        x = dstX;
        y = dstY;
        left = DURATION;

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
        }
    }
}
