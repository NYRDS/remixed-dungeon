package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class ShamanArmor extends ElfArmor {

	public ShamanArmor()
	{
        name = StringsManager.getVar(R.string.ElfArmor_Name);
		image = 19;
		hasHelmet = true;
		coverHair = true;
	}	
	@Override
	public boolean doEquip(@NotNull Char hero ) {
		if (hero.getSubClass() == HeroSubClass.SHAMAN) {
			return super.doEquip( hero );
		} else {
            GLog.w(StringsManager.getVar(R.string.ElfArmor_NotElf));
			return false;
		}
	}
}