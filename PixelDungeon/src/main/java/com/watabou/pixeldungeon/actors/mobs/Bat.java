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
package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.weapon.enchantments.Leech;
import com.watabou.utils.Random;

public class Bat extends Mob {

	public Bat() {
		hp(ht(30));
		defenseSkill = 15;
		baseSpeed = 2f;
		
		EXP = 7;
		maxLvl = 15;
		
		flying = true;
		
		loot = new PotionOfHealing();
		lootChance = 0.125f;
		
		RESISTANCES.add( Leech.class );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 6, 12 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 16;
	}
	
	@Override
	public int dr() {
		return 4;
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		
		int reg = Math.min( damage, ht() - hp() );
		
		if (reg > 0) {
			hp(hp() + reg);
			getSprite().emitter().burst( Speck.factory( Speck.HEALING ), 1 );
		}
		
		return damage;
	}
}
