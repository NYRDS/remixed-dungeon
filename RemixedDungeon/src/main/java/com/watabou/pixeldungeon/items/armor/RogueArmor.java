
package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class RogueArmor extends ClassArmor {
	
	{
		image = 8;
	}
	
	@Override
	public String special() {
		return "RogueArmor_ACSpecial";
	}
	
	@Override
	public void doSpecial(@NotNull Char user) {
		user.selectCell( teleporter );
	}
	
	@Override
	public boolean doEquip(@NotNull Char hero ) {
		if (hero.getHeroClass() == HeroClass.ROGUE) {
			return super.doEquip( hero );
		} else {
            GLog.w(StringsManager.getVar(R.string.RogueArmor_NotRogue));
			return false;
		}
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.RogueArmor_Desc);
    }

	protected static CellSelector.Listener teleporter = new TeleportCellListener();

}