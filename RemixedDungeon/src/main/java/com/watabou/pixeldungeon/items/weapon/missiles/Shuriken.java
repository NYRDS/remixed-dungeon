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
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class Shuriken extends MissileWeapon {

	public Shuriken() {
		this( 1 );
	}
	
	public Shuriken( int number ) {
		super();
		
		image = ItemSpriteSheet.SHURIKEN;
		
		setSTR(13);
		
		MIN = 2;
		MAX = 6;
		
		DLY = 0.5f;
		
		quantity(number);
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.Shuriken_Info);
    }
	
	@Override
	public Item random() {
		quantity(Random.Int( 5, 15 ));
		return this;
	}
	
	@Override
	public int price() {
		return 15 * quantity();
	}
	
	@Override
	public boolean isFliesStraight() {
		return false;
	}

	@Override
	public boolean isFliesFastRotating() {
		return true;
	}
}
