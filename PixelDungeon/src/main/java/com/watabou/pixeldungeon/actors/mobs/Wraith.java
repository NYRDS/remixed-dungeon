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

import com.nyrds.retrodungeon.mobs.common.IDepthAdjustable;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.sprites.WraithSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Wraith extends Mob implements IDepthAdjustable {

	private static final float SPAWN_DELAY	= 2f;
	
	private int level;
	
	public Wraith() {
		spriteClass = WraithSprite.class;
		
		hp(ht(1));
		exp = 0;
		
		flying = true;
		
		IMMUNITIES.add( Death.class );
		IMMUNITIES.add( Terror.class );
	}
	
	private static final String LEVEL = "level";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEVEL, level );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		level = bundle.getInt( LEVEL );
		adjustStats( level );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 3 + level );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 10 + level;
	}
	
	public void adjustStats( int level ) {
		this.level = level;
		defenseSkill = attackSkill( null ) * 5;
		enemySeen = true;
	}
	
	@Override
	public boolean reset() {
		setState(WANDERING);
		return true;
	}
	
	public static void spawnAround( int pos ) {
		for (int n : Level.NEIGHBOURS4) {
			int cell = pos + n;
			if (Dungeon.level.passable[cell] && Actor.findChar( cell ) == null) {
				spawnAt( cell );
			}
		}
	}
	
	public static Wraith spawnAt( int pos ) {
		if (Dungeon.level.passable[pos] && Actor.findChar( pos ) == null) {
			
			Wraith w = new Wraith();
			w.adjustStats( Dungeon.depth );
			w.setPos(pos);
			w.setState(w.HUNTING);
			Dungeon.level.spawnMob(w, SPAWN_DELAY );
			
			w.getSprite().alpha( 0 );
			w.getSprite().getParent().add( new AlphaTweener( w.getSprite(), 1, 0.5f ) );
			
			w.getSprite().emitter().burst( ShadowParticle.CURSE, 5 );
			
			return w;
		} else {
			return null;
		}
	}	
}
