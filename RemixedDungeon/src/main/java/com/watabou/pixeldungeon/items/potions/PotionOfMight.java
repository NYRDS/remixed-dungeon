/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
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
