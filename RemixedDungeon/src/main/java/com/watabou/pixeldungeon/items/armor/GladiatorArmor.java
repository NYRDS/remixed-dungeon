package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class GladiatorArmor extends WarriorArmor {

	{
        name = StringsManager.getVar(R.string.WarriorArmor_Name);
		image = 7;
		hasHelmet = true;
		coverHair = true;
	}
	@Override
	public boolean doEquip(@NotNull Char hero ) {
		if (hero.getSubClass() == HeroSubClass.GLADIATOR) {
			return super.doEquip( hero );
		} else {
            GLog.w(StringsManager.getVar(R.string.WarriorArmor_NotWarrior));
			return false;
		}
	}
}