package com.watabou.pixeldungeon.items.potions;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.utils.GLog;

public class PotionOfMana extends Potion {

	{
		labelIndex = 12;
	}

	@Override
	protected void apply(Char chr) {
		setKnown();

        GLog.i(StringsManager.getVar(R.string.Drink_Message));

		chr.setSkillPoints(chr.getSkillPoints() + chr.getSkillPointsMax() / 3);
		SpellSprite.show(chr, SpellSprite.FOOD);
	}

	@Override
	public String desc() {
        return StringsManager.getVar(R.string.ManaPotion_Info);
    }
}
