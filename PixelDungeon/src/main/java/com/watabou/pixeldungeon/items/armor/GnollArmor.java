package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.utils.GLog;

/**
 * Created by mike on 10.01.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class GnollArmor extends ClassArmor {

    private static final String AC_SPECIAL = Game.getVar(R.string.GnollArmor_ACSpecial);

    private static final String TXT_NOT_GNOLL = Game.getVar(R.string.GnollArmor_NotGnoll);

    {
        image = 25;
        coverHair = false;
        hasCollar = false;
        hasHelmet = false;
    }

    @Override
    public String special() {
        return AC_SPECIAL;
    }

    @Override
    public void doSpecial() {

    }

    @Override
    public boolean doEquip( Hero hero ) {
        if (hero.heroClass == HeroClass.GNOLL) {
            return super.doEquip( hero );
        } else {
            GLog.w( TXT_NOT_GNOLL);
            return false;
        }
    }
}
