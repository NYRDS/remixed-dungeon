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
package com.watabou.pixeldungeon.items.keys;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class IronKey extends Key {

	public static int curDepthQuantity = 0;
	
	{
		image = ItemSpriteSheet.IRON_KEY;
	}
	
	@Override
	public boolean collect(@NotNull Bag bag ) {
		boolean result = super.collect( bag );
		if (result && getDepth() == Dungeon.depth && Dungeon.hero != null) {
			Dungeon.hero.getBelongings().countIronKeys();
		}
		return result;
	}
	
	@Override
	public void onDetach( ) {
		if (getDepth() == Dungeon.depth) {
			Dungeon.hero.getBelongings().countIronKeys();
		}
	}
	
	@NotNull
    @Override
	public String toString() {
		return Utils.format( Game.getVar(R.string.IronKey_FromDepth), getDepth());
	}
}
