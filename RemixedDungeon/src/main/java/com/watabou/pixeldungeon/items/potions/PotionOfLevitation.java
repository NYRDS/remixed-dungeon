
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Levitation;
import com.watabou.pixeldungeon.items.food.RottenFood;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.utils.GLog;

public class PotionOfLevitation extends UpgradablePotion {

	{
		labelIndex = 2;
	}

	@Override
	protected void apply(Char hero ) {
		setKnown();
		Buff.affect( hero, Levitation.class, (float) (Levitation.DURATION *qualityFactor()));
        GLog.i(StringsManager.getVar(R.string.PotionOfLevitation_Apply));
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.PotionOfLevitation_Info);
    }

	@Override
	public int basePrice() {
		return 35;
	}
	
	@Override
	protected void moistenArrow(Arrow arrow, Char owner) {
		detachMoistenItems(arrow, (int) (10*qualityFactor()));
        GLog.i(StringsManager.getVar(R.string.Potion_ItemFliesAway), arrow.name());
		moistenEffective(owner);
	}
	
	@Override
	protected void moistenScroll(Scroll scroll, Char owner) {
		detachMoistenItems(scroll, (int) (3*qualityFactor()));
        GLog.i(StringsManager.getVar(R.string.Potion_ItemFliesAway), scroll.name());
		moistenEffective(owner);
	}
	
	@Override
	protected void moistenRottenFood(RottenFood rfood, Char owner) {
		detachMoistenItems(rfood, (int) (1*qualityFactor()));

        GLog.i(StringsManager.getVar(R.string.Potion_ItemFliesAway), rfood.name());
		moistenEffective(owner);
	}
}
