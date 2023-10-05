
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;

public class PotionOfExperience extends Potion {

	{
		labelIndex = 11;
	}

	@Override
	protected void apply(Char chr ) {
		setKnown();
		if(chr instanceof Hero) {
			Hero hero = (Hero)chr;
			hero.earnExp(hero.maxExp() - hero.getExp());
		}
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.PotionOfExperience_Info);
    }
	
	@Override
	public int price() {
		return isKnown() ? 80 * quantity() : super.price();
	}
}
