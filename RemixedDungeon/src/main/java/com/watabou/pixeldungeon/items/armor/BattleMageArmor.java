package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.utils.GLog;

public class BattleMageArmor extends MageArmor {
    {
        name = Game.getVar(R.string.MageArmor_Name);
        hasCollar = true;
        image = 12;
    }

    @Override
    public boolean doEquip(Hero hero) {
        if (hero.subClass == HeroSubClass.BATTLEMAGE) {
            return super.doEquip(hero);
        } else {
            GLog.w(Game.getVar(R.string.MageArmor_NotMage));
            return false;
        }
    }
}