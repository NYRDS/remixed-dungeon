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

import com.watabou.noosa.Game;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.sprites.RatKingSprite;
import com.watabou.utils.Bundle;

public class RatKing extends NPC {

	private static final String ANGER = "anger";
	
	private int anger = 0;
	
	public RatKing() {
		spriteClass = RatKingSprite.class;
		state  = SLEEPEING;
		defenseSkill = 20;
		
		hp(ht(30));
		EXP = 1;
	}
	
	private boolean friendly(){
		return anger < 2;
	}
	
	@Override
	public float speed() {
		return 2f;
	}
	
	@Override
	protected Char chooseEnemy() {
		if(friendly()) {
			return DUMMY;
		} else {
			return super.chooseEnemy();
		}
	}
	
	@Override
	public void damage( int dmg, Object src ) {
		if(friendly()){anger++;} else {
			super.damage(dmg, src);
		}
	}
	
	@Override
	public void add( Buff buff ) {
		if(friendly()){} else {
			super.add(buff);
		}
	}
	
	@Override
	public boolean reset() {
		return true;
	}
	
	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( pos, hero.pos );
		
		if (!friendly()) {
			return false;
		}
		
		if (state == SLEEPEING) {
			notice();
			say(Game.getVar(R.string.RatKing_Info1));
			state = WANDERING;
		} else {
			anger++;
			if(friendly()) {
				say(Game.getVar(R.string.RatKing_Info2));
			} else {
				hostile = true;
				state = HUNTING;
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
