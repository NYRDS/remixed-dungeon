package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class ScoutArmor extends ElfArmor {
	
	public ScoutArmor()
	{
		name = Game.getVar(R.string.ElfArmor_Name);
		image = 18;
		hasHelmet = false;
	}	

	@Override
	public boolean doEquip(@NotNull Char hero ) {
		if (hero.getSubClass() == HeroSubClass.SCOUT) {
			return super.doEquip( hero );
		} else {
			GLog.w( Game.getVar(R.string.ElfArmor_NotElf) );
			return false;
		}
	}
}