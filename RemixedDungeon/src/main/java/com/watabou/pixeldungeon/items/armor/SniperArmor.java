package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class SniperArmor extends HuntressArmor {

	{
        name = StringsManager.getVar(R.string.HuntressArmor_Name);
		image = 15;
		hasHelmet = true;
	}
	
	@Override
	public boolean doEquip(@NotNull Char hero ) {
		if (hero.getSubClass() == HeroSubClass.SNIPER) {
			return super.doEquip( hero );
		} else {
            GLog.w(StringsManager.getVar(R.string.HuntressArmor_NotHuntress));
			return false;
		}
	}
}