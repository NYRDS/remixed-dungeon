package com.nyrds.pixeldungeon.items;

import androidx.annotation.Keep;

import com.nyrds.Packable;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;

public class Carcass extends Item {
    @Packable
    Char owner;

    @Keep
    public Carcass() {
    }
    public Carcass(Char owner) {
        this.owner = owner;
    }

    @Override
    public Image getCustomImage() {
        return owner.getSprite().carcass();
    }
}
