package com.nyrds.pixeldungeon.items.guts.weapon.ranged;

import com.watabou.pixeldungeon.items.weapon.melee.KindOfBow;

abstract class Crossbow extends KindOfBow {

    public Crossbow(int tier, float acu, float dly) {
        super(tier, acu, dly);
        animation_class = CROSSBOW_ATTACK;
    }

}
