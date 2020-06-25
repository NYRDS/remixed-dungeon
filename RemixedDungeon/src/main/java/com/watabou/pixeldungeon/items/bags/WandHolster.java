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
package com.watabou.pixeldungeon.items.bags;

import com.nyrds.pixeldungeon.utils.CharsList;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

import org.jetbrains.annotations.NotNull;

public class WandHolster extends Bag {

	{
		image = ItemSpriteSheet.HOLSTER;
	}
	
	@Override
	public boolean grab(@NotNull Item item ) {
		return super.grab(item) || item instanceof Wand;
	}
	
	@Override
	public boolean collect(@NotNull Bag container ) {
		if (super.collect( container )) {
			if (getOwner() != CharsList.DUMMY) {
				for (Item item : items) {
					if(item instanceof Wand) {
						((Wand) item).charge(getOwner());
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int price() {
		return 50;
	}
}
