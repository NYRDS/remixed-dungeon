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

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.sprites.PiranhaSprite;

public class Piranha extends Mob {
	
	public Piranha() {
		spriteClass = PiranhaSprite.class;

		walkingType = WalkingType.WATER;

		hp(ht(10 + Dungeon.depth * 5));
		baseDefenseSkill = 10 + Dungeon.depth * 2;
		baseAttackSkill = 20 + Dungeon.depth * 2;

		dr = Dungeon.depth;

		dmgMin = Dungeon.depth;
		dmgMax = 4 + Dungeon.depth * 2;

		baseSpeed = 2f;
		
		exp = 0;

		collect(ItemFactory.itemByName("RawFish"));

		addImmunity( Burning.class );
		addImmunity( Paralysis.class );
		addImmunity( ToxicGas.class );
		addImmunity( Roots.class );
		addImmunity( Frost.class );
	}
	
	@Override
    public boolean act() {
		if (!level().water[getPos()]) {
			die( null );
			return true;
		} else {
			return super.act();
		}
	}

	@Override
	public void die(NamedEntityKind cause) {
		super.die( cause );
		
		Statistics.piranhasKilled++;
		Badges.validatePiranhasKilled();
	}
	
	@Override
	public boolean reset() {
		return true;
	}
}
