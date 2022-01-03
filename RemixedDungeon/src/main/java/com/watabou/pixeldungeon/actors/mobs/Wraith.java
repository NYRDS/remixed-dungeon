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

import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.mobs.common.IDepthAdjustable;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;

public class Wraith extends Mob implements IDepthAdjustable {

	private static final float SPAWN_DELAY	= 2f;
	
	private int level;
	
	public Wraith() {
		hp(ht(1));
		exp = 0;
		
		flying = true;

		level = Dungeon.depth;

		addImmunity( Death.class );
		addImmunity( Terror.class );
		adjustStats( level );
	}

	@Override
	public int attackSkill( Char target ) {
		return 10 + level;
	}
	
	public void adjustStats( int level ) {
		this.level = level;

		dmgMin = 1;
		dmgMax = 3 + level;

		baseAttackSkill = 10 + level;
		baseDefenseSkill = attackSkill( null ) * 5;
		enemySeen = true;
	}
	
	@Override
	public boolean reset() {
		setState(MobAi.getStateByClass(Wandering.class));
		return true;
	}
	
	public static void spawnAround( int pos ) {
		for (int n : Level.NEIGHBOURS4) {
			int cell = pos + n;
				spawnAt( cell );
		}
	}
	
	public static Wraith spawnAt( int pos ) {
		final Level level = Dungeon.level;
		Wraith w = new Wraith();

		if (w.canSpawnAt(level, pos)) {
			w.setPos(pos);
			w.setState(MobAi.getStateByClass(Hunting.class));
			level.spawnMob(w, SPAWN_DELAY );

			final CharSprite sprite = w.getSprite();

			sprite.alpha( 0 );
			GameScene.addToMobLayer( new AlphaTweener(sprite, 1, 0.5f ) );
			
			sprite.emitter().burst( ShadowParticle.CURSE, 5 );
			
			return w;
		} else {
			return null;
		}
	}	
}
