package com.watabou.pixeldungeon.items.wands;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.ui.QuickSlot;

public class Charger extends Buff {
    private static final float TIME_TO_CHARGE = 40f;

    private Wand wand;

    public Charger(Wand wand) {
        this.wand = wand;
    }

    @Override
    public boolean dontPack(){
        return true;
    }

    @Override
    public boolean attachTo(Char target) {
        super.attachTo(target);
        delay();

        return true;
    }

    @Override
    public boolean act() {

        if (wand.curCharges() < wand.maxCharges()) {
            wand.curCharges(wand.curCharges() + 1);
            QuickSlot.refresh(target);
        }

        delay();

        return true;
    }

    protected void delay() {
        float time2charge = target.getHeroClass() == HeroClass.MAGE ? TIME_TO_CHARGE
                / (float) Math.sqrt(1 + wand.effectiveLevel())
                : TIME_TO_CHARGE;
        spend(time2charge);
    }
}
