package com.nyrds.pixeldungeon.items.common;

import com.nyrds.Packable;
import com.watabou.pixeldungeon.items.weapon.missiles.Tamahawk;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * Created by mike on 31.01.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class GnollTamahawk extends Tamahawk {

    @Packable
    private int imageIndex;

    public GnollTamahawk() {
        this(1);
    }

    public GnollTamahawk(int quantity) {
        STR = 15;

        MIN = 2;
        MAX = 15;

        quantity(quantity);

        imageFile = "items/gnoll_tamahawks.png";
        image = imageIndex = Random.Int(0,8);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        image = imageIndex;
    }
}
