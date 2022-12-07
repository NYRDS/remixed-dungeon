package com.watabou.pixeldungeon.items.armor;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.utils.GLog;

public class GladiatorArmor extends WarriorArmor {

	{
		name = Game.getVar(R.string.WarriorArmor_Name);
		image = 7;
		hasHelmet = true;
		coverHair = true;
	}
	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.subClass == HeroSubClass.GLADIATOR) {
			return super.doEquip( hero );
		} else {
			GLog.w( TXT_NOT_WARRIOR );
			return false;
		}
	}
}