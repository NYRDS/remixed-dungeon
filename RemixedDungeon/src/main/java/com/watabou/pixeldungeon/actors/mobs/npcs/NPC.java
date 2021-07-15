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
package com.watabou.pixeldungeon.actors.mobs.npcs;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Passive;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public abstract class NPC extends Mob {
	protected NPC() {
		hp(ht(1));
		exp = 0;

		setState(MobAi.getStateByClass(Passive.class));
		
		fraction = Fraction.NEUTRAL;
		
		gender = Utils.MASCULINE;
	}

	@Override
	public boolean friendly(@NotNull Char chr) {
		if(fraction.belongsTo(Fraction.NEUTRAL)) {
			return true;
		} else {
			return super.friendly(chr);
		}
	}

	@Override
	public void beckon( int cell ) {
	}
	
	@Override
	public boolean interact(final Char hero){
		swapPosition(hero);
		return true;
	}

	@Override
	public boolean canBePet() {
		return false;
	}

	public void fromJson(JSONObject mobDesc) throws JSONException, InstantiationException, IllegalAccessException {
		super.fromJson(mobDesc);

		setState(mobDesc.optString("aiState","Passive").toUpperCase(Locale.ROOT));
	}

	public void sayRandomPhrase(int ...phrases) {
		int index = Random.Int(0, phrases.length);
        say(StringsManager.getVar(phrases[index]));
	}

	public boolean exchangeItem(@NotNull Char hero , String itemClass, String rewardClass) {
		Item item = hero.getItem(itemClass);

		if(!item.valid()) {
			return false;
		}

		item.removeItemFrom(hero);

		Item reward = ItemFactory.itemByName(rewardClass);

		if (reward.doPickUp(hero)) {
			GLog.i(Hero.getHeroYouNowHave(), reward.name());
		} else {
			reward.doDrop(hero);
		}
		return true;
	}
}
