package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class SniperArmor extends HuntressArmor {

	{
		name = Game.getVar(R.string.HuntressArmor_Name);
		image = 15;
		hasHelmet = true;
	}
	
	@Override
	public boolean doEquip(@NotNull Char hero ) {
		if (hero.getSubClass() == HeroSubClass.SNIPER) {
			return super.doEquip( hero );
		} else {
			GLog.w( Game.getVar(R.string.HuntressArmor_NotHuntress) );
			return false;
		}
	}
}