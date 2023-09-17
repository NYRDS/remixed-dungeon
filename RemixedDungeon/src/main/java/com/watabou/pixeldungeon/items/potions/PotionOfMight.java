
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;

public class PotionOfMight extends PotionOfStrength {
	
	@Override
	protected void apply(Char chr ) {
		setKnown();
		if(chr instanceof Hero) {
			Hero hero = (Hero) chr;
			hero.STR(hero.STR() + 1);
			hero.ht(hero.ht() + 5);
			hero.heal(5, this);
            hero.showStatus(CharSprite.POSITIVE, StringsManager.getVar(R.string.PotionOfMight_StaApply));
            GLog.p(StringsManager.getVar(R.string.PotionOfMight_Apply));

			Badges.validateStrengthAttained(hero);
		}
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.PotionOfMight_Info);
    }

	@Override
	public int basePrice() {
		return 200;
	}

	@Override
	public int price() {
		return isKnown() ? 200 * quantity() : super.price();
	}
}
