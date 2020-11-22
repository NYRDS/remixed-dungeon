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

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.utils.Random;

public class Golem extends Mob {
	
	public Golem() {

		hp(ht(85));
		baseDefenseSkill = 18;
		baseAttackSkill  = 28;
		
		exp = 12;
		maxLvl = 22;
		
		addResistance( ScrollOfPsionicBlast.class );
		
		addImmunity( Amok.class );
		addImmunity( Terror.class );
		addImmunity( Sleep.class );
		addImmunity( Bleeding.class );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 20, 40 );
	}

	@Override
	protected float _attackDelay() {
		return 1.5f;
	}
	
	@Override
	public int dr() {
		return 12;
	}
	
	@Override
	public void die(NamedEntityKind cause) {
		Imp.Quest.process( this );
		
		super.die( cause );
	}
}
