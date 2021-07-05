package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.game.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class GuardianArmor extends GnollArmor {
	
	{
		name = Game.getVar(R.string.GnollArmor_Name);
		image = 26;
		hasHelmet = true;
		coverHair = true;
	}

	@Override
	public boolean doEquip(@NotNull Char hero ) {
		//if (hero.getSubClass() == HeroSubClass.GUARDIAN) {
		if (hero.getHeroClass() == HeroClass.GNOLL) {
			return super.doEquip( hero );
		} else {
			GLog.w( Game.getVar(R.string.GnollArmor_NotGnoll) );
			return false;
		}
	}
}