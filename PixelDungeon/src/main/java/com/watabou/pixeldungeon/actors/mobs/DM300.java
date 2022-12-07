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

import com.nyrds.retrodungeon.items.chaos.ChaosCrystal;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.particles.ElmoParticle;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.rings.RingOfThorns;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.DM300Sprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class DM300 extends Boss {
	
	public DM300() {
		spriteClass = DM300Sprite.class;
		
		hp(ht(200));
		exp = 30;
		defenseSkill = 18;
		
		float dice = Random.Float();
		if( dice < 0.5 ) {
			loot = new ChaosCrystal();
		} else {
			loot = new RingOfThorns().random();
		}
		
		lootChance = 0.333f;
		
		IMMUNITIES.add( ToxicGas.class );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 18, 24 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 28;
	}
	
	@Override
	public int dr() {
		return 10;
	}
	
	@Override
	public boolean act() {

		GameScene.add( Blob.seed( getPos(), 30, ToxicGas.class ) );
		
		return super.act();
	}
	
	@Override
	public void move( int step ) {
		super.move( step );

		if (Dungeon.level.map[step] == Terrain.INACTIVE_TRAP && hp() < ht()) {
			
			hp(hp() + Random.Int( 1, ht() - hp() ));
			getSprite().emitter().burst( ElmoParticle.FACTORY, 5 );
			
			if (Dungeon.visible[step] && Dungeon.hero.isAlive()) {
				GLog.n(Game.getVar(R.string.DM300_Info1));
			}
		}
		
		int cell = step + Level.NEIGHBOURS8[Random.Int( Level.NEIGHBOURS8.length )];
		
		if (Dungeon.visible[cell]) {
			CellEmitter.get( cell ).start( Speck.factory( Speck.ROCK ), 0.07f, 10 );
			Camera.main.shake( 3, 0.7f );
			Sample.INSTANCE.play( Assets.SND_ROCKS );

			if (Dungeon.level.water[cell]) {
				GameScene.ripple( cell );
			} else if (Dungeon.level.map[cell] == Terrain.EMPTY) {
				Dungeon.level.set( cell, Terrain.EMPTY_DECO );
				GameScene.updateMap( cell );
			}
		}

		Char ch = Actor.findChar( cell );
		if (ch != null && ch != this) {
			Buff.prolong( ch, Paralysis.class, 2 );
		}
	}
	
	@Override
	public void die( Object cause ) {
		
		super.die( cause );
		
		GameScene.bossSlain();
		Dungeon.level.drop( new SkeletonKey(), getPos() ).sprite.drop();
		
		Badges.validateBossSlain(Badges.Badge.BOSS_SLAIN_3);
		
		yell(Game.getVar(R.string.DM300_Info2));
	}
	
	@Override
	public void notice() {
		super.notice();
		yell(Game.getVar(R.string.DM300_Info3));
	}
}
