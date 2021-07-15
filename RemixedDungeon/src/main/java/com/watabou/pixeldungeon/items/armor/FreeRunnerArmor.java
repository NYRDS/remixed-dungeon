package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class FreeRunnerArmor extends RogueArmor {

	{
        name = StringsManager.getVar(R.string.RogueArmor_Name);
		image = 9;
		hasHelmet = true;
	}
	@Override
	public boolean doEquip(@NotNull Char hero ) {
		if (hero.getSubClass() == HeroSubClass.FREERUNNER) {
			return super.doEquip( hero );
		} else {
            GLog.w(StringsManager.getVar(R.string.RogueArmor_NotRogue));
			return false;
		}
	}
}