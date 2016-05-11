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
package com.nyrds.pixeldungeon.items.guts.weapon.ranged;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.melee.Bow;

public class WoodenCrossbow extends Bow {

	public WoodenCrossbow() {

		super( 2, 1.2f, 1.6f );
		imageFile = "items/ranged.png";
		image = 1;
	}

	@Override
	public Item burn(int cell) {
		return null;
	}

	@Override
	public double acuFactor() {
		return 1 + level() * 0.15;
	}

	@Override
	public double dmgFactor() {
		return 1 + level() * 0.35;
	}
}
