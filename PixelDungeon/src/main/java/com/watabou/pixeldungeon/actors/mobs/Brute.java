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

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.sprites.BruteSprite;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Brute extends Mob {

	private static final String TXT_ENRAGED = Game.getVar(R.string.Brute_Enraged);
	
	public Brute() {
		spriteClass = BruteSprite.class;
		
		hp(ht(40));
		defenseSkill = 15;
		
		exp = 8;
		maxLvl = 15;
		
		loot = Gold.class;
		lootChance = 0.5f;
		
		IMMUNITIES.add( Terror.class );
	}
	
	private boolean enraged = false;
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		enraged = hp() < ht() / 4;
	}
	
	@Override
	public int damageRoll() {
		return enraged ?
			Random.NormalIntRange( 10, 40 ) :	
			Random.NormalIntRange( 8, 18 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 20;
	}
	
	@Override
	public int dr() {
		return 8;
	}
	
	@Override
	public void damage( int dmg, Object src ) {
		super.damage( dmg, src );
		
		if (isAlive() && !enraged && hp() < ht() / 4) {
			enraged = true;
			spend( TICK );
			if (Dungeon.visible[getPos()]) {
				GLog.w( TXT_ENRAGED, getName() );
				getSprite().showStatus( CharSprite.NEGATIVE, Game.getVar(R.string.Brute_StaEnraged));
			}
		}
	}
}
