package com.watabou.pixeldungeon.items.armor;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.utils.GLog;

public class ScoutArmor extends ElfArmor {
	
	public ScoutArmor()
	{
		name = Game.getVar(R.string.ElfArmor_Name);
		image = 18;
		hasHelmet = false;
	}	

	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.subClass == HeroSubClass.SCOUT) {
			return super.doEquip( hero );
		} else {
			GLog.w( TXT_NOT_ELF );
			return false;
		}
	}
}