package com.watabou.pixeldungeon.actors.hero;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.items.bags.Bag;

class Backpack extends Bag {
    {
        name = Game.getVar(R.string.Belongings_Name);
        size = Belongings.BACKPACK_SIZE;
    }
}
