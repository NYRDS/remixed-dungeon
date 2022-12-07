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

import com.nyrds.retrodungeon.mobs.npc.PlagueDoctorNPC;
import com.nyrds.retrodungeon.mobs.npc.ScarecrowNPC;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.items.quest.RatSkull;
import com.watabou.utils.Random;

public class Rat extends Mob {

	public Rat() {
		hp(ht(8));
		defenseSkill = 3;
		
		maxLvl = 7;
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 5 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 8;
	}
	
	@Override
	public int dr() {
		return 1;
	}

	@Override
	protected boolean canAttack(Char enemy) {
		if(enemy.buff(RatSkull.RatterAura.class) != null) {
			setState(FLEEING);
			if(buff(Terror.class)==null) {
				new Flare(5, 32).color(0xFF0000, true).show(getSprite(), 2f);
				Terror terror = Buff.affect(this, Terror.class, Terror.DURATION);
				terror.source = enemy;
				return false;
			}
		}
		return super.canAttack(enemy);
}

	@Override
	public void die( Object cause ) {
		ScarecrowNPC.Quest.process( getPos() );
		Ghost.Quest.process( getPos() );
		PlagueDoctorNPC.Quest.process( getPos() );
		
		super.die( cause );
	}

}
