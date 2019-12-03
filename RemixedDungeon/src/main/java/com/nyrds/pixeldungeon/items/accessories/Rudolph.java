package com.nyrds.pixeldungeon.items.accessories;

import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;

/**
 * Created by DeadDie on 26.05.2016
 */
public class Rudolph extends Accessory{

    {
        coverHair = true;
        image = 15;
    }

    @Override
    public boolean usableBy(Hero hero) {
        if (hero.getHeroClass().equals(HeroClass.GNOLL)) {
            return false;
        }
        return super.usableBy(hero);
    }
}
