package com.watabou.pixeldungeon.items.armor;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.utils.GLog;

public class SniperArmor extends HuntressArmor {

	{
		name = Game.getVar(R.string.HuntressArmor_Name);
		image = 15;
		hasHelmet = true;
	}
	
	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.subClass == HeroSubClass.SNIPER) {
			return super.doEquip( hero );
		} else {
			GLog.w( TXT_NOT_HUNTRESS );
			return false;
		}
	}
}