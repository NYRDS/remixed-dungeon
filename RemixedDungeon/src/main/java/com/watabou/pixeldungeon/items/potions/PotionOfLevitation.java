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
import com.watabou.noosa.Game;
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
		GLog.i(Game.getVar(R.string.PotionOfLevitation_Apply));
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.PotionOfLevitation_Info);
	}

	@Override
	public int basePrice() {
		return 35;
	}
	
	@Override
	protected void moistenArrow(Arrow arrow, Char owner) {
		detachMoistenItems(arrow, (int) (10*qualityFactor()));
		GLog.i(Game.getVar(R.string.Potion_ItemFliesAway) , arrow.name());
		moistenEffective();
	}
	
	@Override
	protected void moistenScroll(Scroll scroll, Char owner) {
		detachMoistenItems(scroll, (int) (3*qualityFactor()));
		GLog.i(Game.getVar(R.string.Potion_ItemFliesAway) , scroll.name());
		moistenEffective();
	}
	
	@Override
	protected void moistenRottenFood(RottenFood rfood, Char owner) {
		detachMoistenItems(rfood, (int) (1*qualityFactor()));
		
		GLog.i(Game.getVar(R.string.Potion_ItemFliesAway) , rfood.name());
		moistenEffective();
	}
}
