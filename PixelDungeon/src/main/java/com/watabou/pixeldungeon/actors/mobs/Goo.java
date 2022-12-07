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

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Ooze;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.GooSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Goo extends Boss {

	private static final float PUMP_UP_DELAY	= 2.2f;
	
	public Goo() {
		hp(ht(68));
		exp = 9;
		defenseSkill = 12;
		spriteClass = GooSprite.class;

		loot = Generator.random(Generator.Category.POTION);

		lootChance = 0.8f;
		
		RESISTANCES.add( ToxicGas.class );
	}
	
	private static final String GOO_PUMPED_STATE = "goo_pumped_state";
	
	private boolean pumpedUp = false;
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		
		bundle.put(GOO_PUMPED_STATE, pumpedUp);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		
		super.restoreFromBundle( bundle );
		pumpedUp = bundle.getBoolean(GOO_PUMPED_STATE);
	}
	
	@Override
	public int damageRoll() {
		if (pumpedUp) {
			return Random.NormalIntRange( 7, 21 );
		} else {
			return Random.NormalIntRange( 4, 11 );
		}
	}
	
	@Override
	public int attackSkill( Char target ) {
		return pumpedUp ? 26 : 11;
	}
	
	@Override
	public int dr() {
		return 2;
	}
	
	@Override
	public boolean act() {
		
		if (Dungeon.level.water[getPos()] && hp() < ht()) {
			getSprite().emitter().burst( Speck.factory( Speck.HEALING ), 1 );
			hp(hp() + 1);
		}
		
		return super.act();
	}
	
	@Override
	protected boolean canAttack( Char enemy ) {
		return pumpedUp ? distance( enemy ) <= 2 : super.canAttack(enemy);
	}
	
	@Override
	public int attackProc(@NonNull Char enemy, int damage ) {
		if (Random.Int( 3 ) == 0) {
			Buff.affect( enemy, Ooze.class );
			enemy.getSprite().burst( 0x000000, 5 );
		}
		
		if (pumpedUp) {
			Camera.main.shake( 3, 0.2f );
		}
		
		return damage;
	}
	
	@Override
	protected boolean doAttack( Char enemy ) {		
		if (pumpedUp || Random.Int( 3 ) > 0) {
		
			return super.doAttack( enemy );

		} else {
			
			pumpedUp = true;
			spend( PUMP_UP_DELAY );
			
			((GooSprite)getSprite()).pumpUp();
			
			if (Dungeon.visible[getPos()]) {
				getSprite().showStatus( CharSprite.NEGATIVE, Game.getVar(R.string.Goo_StaInfo1));
				GLog.n(Game.getVar(R.string.Goo_Info1));
			}
				
			return true;
		}
	}
	
	@Override
	public boolean attack(@NonNull Char enemy ) {
		boolean result = super.attack( enemy );
		pumpedUp = false;
		return result;
	}
	
	@Override
	protected boolean getCloser( int target ) {
		pumpedUp = false;
		return super.getCloser( target );
	}
	
	@Override
	public void move( int step ) {
		Dungeon.level.seal();
		super.move( step );
	}
	
	@Override
	public void die( Object cause ) {
		
		super.die( cause );
		
		Dungeon.level.unseal();
		
		GameScene.bossSlain();
		Dungeon.level.drop( new SkeletonKey(), getPos() ).sprite.drop();
		
		Badges.validateBossSlain(Badges.Badge.BOSS_SLAIN_1);
		
		yell(Game.getVar(R.string.Goo_Info2));
	}
	
	@Override
	public void notice() {
		super.notice();
		yell(Game.getVar(R.string.Goo_Info3));
	}

	@Override
	public boolean zap(@NonNull Char enemy) {
		pumpedUp = false;
		return true;
	}
}
