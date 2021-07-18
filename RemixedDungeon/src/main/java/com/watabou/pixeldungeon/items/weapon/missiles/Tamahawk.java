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

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class Tamahawk extends MissileWeapon {

	public Tamahawk() {
		this( 1 );
	}
	
	public Tamahawk( int number ) {
		super();
		image = ItemSpriteSheet.TOMAHAWK;
		
		setSTR(17);
		
		MIN = 4;
		MAX = 20;
		
		quantity(number);
	}
	
	@Override
	public void attackProc(Char attacker, Char defender, int damage ) {
		super.attackProc( attacker, defender, damage );
		Buff.affect( defender, Bleeding.class ).level( damage );
	}	

	@Override
	public Item random() {
		quantity(Random.Int( 5, 12 ));
		return this;
	}
	
	@Override
	public int price() {
		return 20 * quantity();
	}

	@Override
	public String getAttackAnimationClass() {
		return SWORD_ATTACK;
	}

	@Override
	public String getVisualName() {
		return "Tomahawk";
	}

}
