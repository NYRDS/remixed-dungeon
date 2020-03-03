package com.nyrds.pixeldungeon.items;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.items.EquipableItem;

public class DummyItem extends EquipableItem {
    @Override
    public Belongings.Slot slot(Belongings belongings) {
        return Belongings.Slot.NONE;
    }

    @Override
    public float impactDelayFactor(Char user, float delayFactor) {
        return delayFactor;
    }

    @Override
    public float impactAccuracyFactor(Char user, float accuracyFactor) {
        return accuracyFactor;
    }

    @Override
    public boolean dontPack() {
        return true;
    }
}
