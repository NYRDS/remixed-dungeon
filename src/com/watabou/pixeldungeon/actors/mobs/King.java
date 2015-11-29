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

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.ArmorKit;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.items.wands.WandOfDisintegration;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.levels.CityBossLevel;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.KingSprite;
import com.watabou.pixeldungeon.sprites.UndeadSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class King extends Boss {
	
	private static final int MAX_ARMY_SIZE	= 5;
	
	public King() {
		spriteClass = KingSprite.class;
		
		hp(ht(300));
		EXP = 40;
		defenseSkill = 25;
		
		Undead.count = 0;
		
		RESISTANCES.add( ToxicGas.class );
		RESISTANCES.add( WandOfDisintegration.class );
		
		IMMUNITIES.add( Paralysis.class );
	}
	
	private boolean nextPedestal = true;
	
	private static final String PEDESTAL = "pedestal";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( PEDESTAL, nextPedestal );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		nextPedestal = bundle.getBoolean( PEDESTAL );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 20, 38 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 32;
	}
	
	@Override
	public int dr() {
		return 14;
	}
	
	
	@Override
	protected boolean getCloser( int target ) {
		return canTryToSummon() ? 
			super.getCloser( ((CityBossLevel)(Dungeon.level)).pedestal( nextPedestal ) ) : 
			super.getCloser( target );
	}
	
	@Override
	protected boolean canAttack( Char enemy ) {
		return canTryToSummon() ? 
			getPos() == ((CityBossLevel)(Dungeon.level)).pedestal( nextPedestal ) : 
			Dungeon.level.adjacent( getPos(), enemy.getPos() );
	}
	
	private boolean canTryToSummon() {
		if (Undead.count < maxArmySize()) {
			Char ch = Actor.findChar( ((CityBossLevel)(Dungeon.level)).pedestal( nextPedestal ) );
			return ch == this || ch == null;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean attack( Char enemy ) {
		if (canTryToSummon() && getPos() == ((CityBossLevel)(Dungeon.level)).pedestal( nextPedestal )) {
			summon();
			return true;
		} else {
			if (Actor.findChar( ((CityBossLevel)(Dungeon.level)).pedestal( nextPedestal ) ) == enemy) {
				nextPedestal = !nextPedestal;
			}
			return super.attack(enemy);
		}
	}
	
	@Override
	public void die( Object cause ) {
		GameScene.bossSlain();
		Dungeon.level.drop( new ArmorKit(), getPos() ).sprite.drop();
		Dungeon.level.drop( new SkeletonKey(), getPos() ).sprite.drop();
		
		super.die( cause );
		
		Badges.validateBossSlain();
		
		yell(String.format(Game.getVar(R.string.King_Info1), Dungeon.hero.heroClass.title()));
	}
	
	private int maxArmySize() {
		return 1 + MAX_ARMY_SIZE * (ht() - hp()) / ht();
	}
	
	private void summon() {

		nextPedestal = !nextPedestal;
		
		getSprite().centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.4f, 2 );		
		Sample.INSTANCE.play( Assets.SND_CHALLENGE );
		
		boolean[] passable = Dungeon.level.passable.clone();
		for (Actor actor : Actor.all()) {
			if (actor instanceof Char) {
				passable[((Char)actor).getPos()] = false;
			}
		}
		
		int undeadsToSummon = maxArmySize() - Undead.count;

		PathFinder.buildDistanceMap( getPos(), passable, undeadsToSummon );
		PathFinder.distance[getPos()] = Integer.MAX_VALUE;
		int dist = 1;
		
	undeadLabel:
		for (int i=0; i < undeadsToSummon; i++) {
			do {
				for (int j=0; j < Dungeon.level.getLength(); j++) {
					if (PathFinder.distance[j] == dist) {
						
						Undead undead = new Undead();
						undead.setPos(j);
						GameScene.add(Dungeon.level, undead );
						
						WandOfBlink.appear( undead, j );
						new Flare( 3, 32 ).color( 0x000000, false ).show( undead.getSprite(), 2f ) ;
						
						PathFinder.distance[j] = Integer.MAX_VALUE;
						
						continue undeadLabel;
					}
				}
				dist++;
			} while (dist < undeadsToSummon);
		}
		
		yell(Game.getVar(R.string.King_Info2));
	}
	
	@Override
	public void notice() {
		super.notice();
		yell(Game.getVar(R.string.King_Info3));
	}
	
	public static class Undead extends Mob {
		
		public static int count = 0;
		
		public Undead() {
			spriteClass = UndeadSprite.class;
			
			hp(ht(28));
			defenseSkill = 15;
			
			EXP = 0;
			
			state = WANDERING;
			
			IMMUNITIES.add( Death.class );
			IMMUNITIES.add( Paralysis.class );
		}
		
		@Override
		protected void onAdd() {
			count++;
			super.onAdd();
		}
		
		@Override
		protected void onRemove() {
			count--;
			super.onRemove();
		}
		
		@Override
		public int damageRoll() {
			return Random.NormalIntRange( 12, 16 );
		}
		
		@Override
		public int attackSkill( Char target ) {
			return 16;
		}
		
		@Override
		public int attackProc( Char enemy, int damage ) {
			if (Random.Int( MAX_ARMY_SIZE ) == 0) {
				Buff.prolong( enemy, Paralysis.class, 1 );
			}
			
			return damage;
		}
		
		@Override
		public void damage( int dmg, Object src ) {
			super.damage( dmg, src );
			if (src instanceof ToxicGas) {		
				((ToxicGas)src).clearBlob( getPos() );
			}
		}
		
		@Override
		public void die( Object cause ) {
			super.die( cause );
			
			if (Dungeon.visible[getPos()]) {
				Sample.INSTANCE.play( Assets.SND_BONES );
			}
		}
		
		@Override
		public int dr() {
			return 5;
		}
	}
}
