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
package com.watabou.pixeldungeon.actors.mobs.npcs;

import com.nyrds.retrodungeon.items.common.RatKingCrown;
import com.watabou.noosa.Game;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.sprites.RatKingSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class RatKing extends NPC {

	private static final String ANGER = "anger";
	
	private int anger = 0;
	
	public RatKing() {
		spriteClass = RatKingSprite.class;
		setState(SLEEPING);
		defenseSkill = 20;
		
		hp(ht(30));
		exp = 1;

		lootChance = 1;
		loot = new RatKingCrown();
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 4, 10 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 15;
	}
	
	@Override
	public int dr() {
		return 5;
	}
	
	@Override
	public boolean friendly(Char chr){
		return anger < 2;
	}
	
	@Override
	public float speed() {
		return 2f;
	}
	
	@Override
	protected Char chooseEnemy() {
		if(friendly(null)) {
			return DUMMY;
		} else {
			return super.chooseEnemy();
		}
	}
	
	@Override
	public void damage( int dmg, Object src ) {
		if(friendly(null)){
			anger=2;
		} else {
			super.damage(dmg, src);
		}
	}
	
	@Override
	public void add( Buff buff ) {
		if (!friendly(null)) {
			super.add(buff);
		}
	}
	
	@Override
	public boolean reset() {
		return true;
	}
	
	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );
		
		if (!friendly(null)) {
			return false;
		}
		
		if (getState() == SLEEPING) {
			notice();
			say(Game.getVar(R.string.RatKing_Info1));
			setState(WANDERING);
		} else {
			anger++;
			if(friendly(null)) {
				say(Game.getVar(R.string.RatKing_Info2));
			} else {
				setFraction(Fraction.DUNGEON);

				setState(HUNTING);
				yell(Game.getVar(R.string.RatKing_Info3));
			}
		}
		return true;
	}
	
	@Override
	public void die(Object cause) {
		say(Game.getVar(R.string.RatKing_Died));
		super.die(cause);
	}
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		
		bundle.put(ANGER, anger);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {

		super.restoreFromBundle(bundle);
		
		anger = bundle.getInt(ANGER);
	}	
}
