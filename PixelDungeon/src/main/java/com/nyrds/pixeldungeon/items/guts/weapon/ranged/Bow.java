package com.nyrds.pixeldungeon.items.guts.weapon.ranged;

import com.watabou.pixeldungeon.items.weapon.melee.KindOfBow;

public abstract class Bow extends KindOfBow {
    public Bow(int tier, float acu, float dly) {
        super(tier, acu, dly);
        animation_class = BOW_ATTACK;
    }
}
