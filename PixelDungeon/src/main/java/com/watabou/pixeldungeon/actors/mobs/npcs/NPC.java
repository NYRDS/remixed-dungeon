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

import com.nyrds.retrodungeon.mechanics.quest.Quest;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public abstract class NPC extends Mob {

	public Quest quest = null;

	protected NPC() {
		hp(ht(1));
		exp = 0;

		setState(PASSIVE);
		
		fraction = Fraction.NEUTRAL;
		
		gender = Utils.MASCULINE;
	}
	
	protected void throwItem() {
		Heap heap = Dungeon.level.getHeap( getPos() );
		if (heap != null) {
			int n;
			do {
				n = getPos() + Level.NEIGHBOURS8[Random.Int( 8 )];
			} while (!Dungeon.level.passable[n] && !Dungeon.level.avoid[n]);
			Dungeon.level.drop( heap.pickUp(), n ).sprite.drop( getPos() );
		}
	}

	@Override
	public boolean friendly(Char chr) {
		return true;
	}

	@Override
	public void beckon( int cell ) {
	}
	
	@Override
	public boolean interact(final Hero hero){
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
}
