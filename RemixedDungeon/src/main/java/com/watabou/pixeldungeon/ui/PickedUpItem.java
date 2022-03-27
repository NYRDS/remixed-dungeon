package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.game.GameLoop;
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

        setX(dstX);
        setY(dstY);
        left = DURATION;

        alpha(1);
    }

    @Override
    public void update() {
        super.update();

        if ((left -= GameLoop.elapsed) <= 0) {
            setVisible(active = false);
        } else {
            float p = left / DURATION;
            setScale((float) Math.sqrt(p));
        }
    }
}
