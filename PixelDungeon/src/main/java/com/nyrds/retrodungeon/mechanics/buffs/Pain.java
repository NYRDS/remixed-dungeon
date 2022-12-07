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
package com.nyrds.retrodungeon.mechanics.buffs;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.effects.Splash;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.PointF;

public class Pain extends Buff {

	protected int level;
	protected int duration;
	
	private static final String LEVEL	= "level";
	private static final String DURATION= "duration";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEVEL, level );
		bundle.put( DURATION, duration );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		level = bundle.getInt( LEVEL );
		duration = bundle.getInt( DURATION );
	}

	public void set( int level, int duration ) {
		this.level = level;
		this.duration = duration;
	}

	@Override
	public int icon() {
		return BuffIndicator.BLEEDING;
	}
	
	@Override
	public String toString() {
		return Game.getVar(R.string.Bleeding_Info);
	}
	
	@Override
	public boolean act() {
		if (target.isAlive()) {
			
			if (duration > 0) {
				
				target.damage( 2 + level, this );
				if (target.getSprite().getVisible()) {
					Splash.at( target.getSprite().center(), -PointF.PI / 8, PointF.PI / 2,
							target.getSprite().blood(), Math.min( 10 * level / target.ht(), 10 ) );
				}
				
				//if (target == Dungeon.hero && !target.isAlive()) {
				//	Dungeon.fail( Utils.format( ResultDescriptions.PAIN, Dungeon.depth ) );
				//	GLog.n(Game.getVar(R.string.Pain_Death));
				//}
				
				spend( TICK );
				duration--;
			} else {
				detach();
			}
			
		} else {
			
			detach();
			
		}
		
		return true;
	}
}
