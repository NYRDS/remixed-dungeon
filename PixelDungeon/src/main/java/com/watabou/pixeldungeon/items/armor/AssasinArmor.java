package com.watabou.pixeldungeon.items.armor;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.utils.GLog;

public class AssasinArmor extends RogueArmor {

	{
        name = Game.getVar(R.string.RogueArmor_Name);
		image = 10;
	}

	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.subClass == HeroSubClass.ASSASSIN) {
			return super.doEquip( hero );
		} else {
			GLog.w( TXT_NOT_ROGUE );
			return false;
		}
	}
}