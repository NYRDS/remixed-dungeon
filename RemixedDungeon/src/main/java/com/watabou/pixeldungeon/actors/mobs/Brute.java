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

import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.items.Gold;

public class Brute extends Mob {

	public Brute() {

		hp(ht(40));
		baseAttackSkill = 15;
		baseDefenseSkill = 20;
		dmgMin = 8;
		dmgMax = 18;

		exp = 8;
		maxLvl = 15;

		dr = 8;

		loot(Gold.class, 0.5f);
		
		addImmunity( Terror.class );
	}

	@Override
	public HeroSubClass getSubClass() {
		return HeroSubClass.BERSERKER;
	}
}
