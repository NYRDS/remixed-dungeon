package com.nyrds.pixeldungeon.items.common;

import androidx.annotation.Keep;

import com.nyrds.Packable;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.weapon.missiles.Tamahawk;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

/**
 * Created by mike on 31.01.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class GnollTamahawk extends Tamahawk {

    @Packable
    private int imageIndex;

    @Keep
    public GnollTamahawk() {
        this(1);
    }

    public GnollTamahawk(int quantity) {
        setSTR(15);

        MIN = 3;
        MAX = 17;

        quantity(quantity);

        imageFile = "items/gnoll_tomahawks.png";
        image = imageIndex = Random.Int(0,8);
    }

    @Override
    protected void onThrow(int cell, @NotNull Char thrower) {
        Char enemy = Actor.findChar( cell );
        if (enemy != null && enemy != thrower) {
            thrower.shoot( enemy, this );
        }
        dropTo( cell, thrower);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        image = imageIndex;
    }

    @Override
    protected boolean notUsableInMelee() {
        return false;
    }

    @Override
    public boolean isUpgradable() {
        return true;
    }
}
