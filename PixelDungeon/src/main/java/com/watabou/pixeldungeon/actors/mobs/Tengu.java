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

import com.nyrds.retrodungeon.mobs.common.IZapper;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.pixeldungeon.Badges.Badge;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.TomeOfMastery;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Tengu extends Boss implements IZapper {

	private static final int JUMP_DELAY = 5;
	
	public Tengu() {
		
		hp(ht(120));
		exp = 20;
		defenseSkill = 20;
		
		RESISTANCES.add( ToxicGas.class );
		RESISTANCES.add( Poison.class );
	}
	
	private int timeToJump = JUMP_DELAY;
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 8, 15 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 20;
	}
	
	@Override
	public int dr() {
		return 5;
	}
	
	@Override
	public void die( Object cause ) {

		if ( Dungeon.hero.heroClass != HeroClass.NECROMANCER){
			Dungeon.level.drop( new TomeOfMastery(), getPos() ).sprite.drop();
		}

		GameScene.bossSlain();
		Dungeon.level.drop( new SkeletonKey(), getPos() ).sprite.drop();
		super.die(cause);
		
		Badges.validateBossSlain(Badge.BOSS_SLAIN_2);

		say(Game.getVar(R.string.Tengu_Info1));
	}
	
	@Override
	protected boolean getCloser( int target ) {
		if (Dungeon.level.fieldOfView[target]) {
			jump();
			return true;
		} else {
			return super.getCloser( target );
		}
	}
	
	@Override
	protected boolean canAttack( Char enemy ) {
		return Ballistica.cast( getPos(), enemy.getPos(), false, true ) == enemy.getPos();
	}
	
	@Override
	protected boolean doAttack( Char enemy ) {
		timeToJump--;
		if (timeToJump <= 0 && Dungeon.level.adjacent( getPos(), enemy.getPos() )) {
			jump();
			return true;
		} else {
			return super.doAttack( enemy );
		}
	}
	
	private void jump() {
		timeToJump = JUMP_DELAY;

		for (int i=0; i < 4; i++) {
			int trapPos = Dungeon.level.getRandomTerrainCell(Terrain.INACTIVE_TRAP);

			if (Dungeon.level.cellValid(trapPos)) {
				Dungeon.level.set( trapPos, Terrain.POISON_TRAP );
				GameScene.updateMap( trapPos );
				ScrollOfMagicMapping.discover( trapPos );
			} else {
				break;
			}
		}

		ArrayList<Integer> candidates = new ArrayList<>();
		int enemyPos = getEnemy().getPos();
		for(int i = 0;i<Dungeon.level.getLength();++i) {
			if(Dungeon.level.fieldOfView[i]
					&& Dungeon.level.passable[i]
					&& !Dungeon.level.adjacent( i, enemyPos )
					&& Actor.findChar(i) == null) {
				candidates.add(i);
			}
		}

		if(candidates.isEmpty()) {
			PotionOfHealing.heal(this, 0.1f);
			spend( 1 / speed() );
			return;
		}

		int newPos = candidates.get(Random.index(candidates));

		getSprite().move( getPos(), newPos );
		move( newPos );
		
		if (Dungeon.visible[newPos]) {
			CellEmitter.get( newPos ).burst( Speck.factory( Speck.WOOL ), 6 );
			Sample.INSTANCE.play( Assets.SND_PUFF );
		}
		
		spend( 1 / speed() );
	}
	
	@Override
	public void notice() {
		super.notice();
		String tenguYell = Game.getVar(R.string.Tengu_Info2);
		if (Dungeon.hero.heroClass.getGender() == Utils.FEMININE) {
			tenguYell = Game.getVar(R.string.Tengu_Info3);
		}
		yell(Utils.format(tenguYell, Dungeon.hero.heroClass.title()));
	}	
}
