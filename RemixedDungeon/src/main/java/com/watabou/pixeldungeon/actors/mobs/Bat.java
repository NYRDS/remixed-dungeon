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
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.weapon.enchantments.Leech;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Bat extends Mob {

	public Bat() {
		hp(ht(30));
		baseDefenseSkill = 15;
		baseAttackSkill  = 16;
		baseSpeed = 2f;
		
		exp = 7;
		maxLvl = 15;
		
		flying = true;
		
		loot(PotionOfHealing.class, 0.125f);

		addResistance( Leech.class );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 6, 12 );
	}
	
	@Override
	public int dr() {
		return 4;
	}
	
	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		
		heal(damage, enemy);

		return damage;
	}
}
