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

import com.nyrds.Packable;
import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.mobs.necropolis.UndeadMob;
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
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.ArmorKit;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.items.wands.WandOfDisintegration;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.KingSprite;
import com.watabou.pixeldungeon.sprites.UndeadSprite;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class King extends Boss {
	
	private static final int MAX_ARMY_SIZE	= 5;

	@Packable
	private int lastPedestal;

	@Packable
	private int targetPedestal;

	public King() {
		spriteClass = KingSprite.class;
		
		hp(ht(300));
		exp = 40;
		defenseSkill = 25;
		
		Undead.count = 0;

		lastPedestal   = -1;
		targetPedestal = -1;

		RESISTANCES.add( ToxicGas.class );
		RESISTANCES.add( WandOfDisintegration.class );
		
		IMMUNITIES.add( Paralysis.class );
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

		Level level = Dungeon.level;
		int x = level.cellX(getPos());
		int y = level.cellY(getPos());

 		targetPedestal = level.getNearestTerrain(x,y, Terrain.PEDESTAL, lastPedestal);

		if(canTryToSummon()) {
			return super.getCloser( targetPedestal );
		}

		return super.getCloser(target);
	}
	
	@Override
	protected boolean canAttack( Char enemy ) {
		return canTryToSummon() ? 
			getPos() == targetPedestal :
			Dungeon.level.adjacent( getPos(), enemy.getPos() );
	}
	
	private boolean canTryToSummon() {
		if (!Dungeon.level.cellValid(targetPedestal)) {
			return false;
		}

		if (Undead.count < maxArmySize()) {
			Char ch = Actor.findChar(targetPedestal);
			return ch == this || ch == null;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean attack(@NonNull Char enemy ) {
		return super.attack(enemy);
	}
	
	@Override
	public void die( Object cause ) {
		GameScene.bossSlain();
		Dungeon.level.drop( new ArmorKit(), getPos() ).sprite.drop();
		Dungeon.level.drop( new SkeletonKey(), getPos() ).sprite.drop();
		
		super.die( cause );
		
		Badges.validateBossSlain(Badges.Badge.BOSS_SLAIN_4);
		
		yell(Utils.format(Game.getVar(R.string.King_Info1), Dungeon.hero.heroClass.title()));
	}
	
	private int maxArmySize() {
		return (int) (1 + MAX_ARMY_SIZE * (ht() - hp()) / ht() * Game.instance().getDifficultyFactor());
	}

	@Override
	public boolean zap(@NonNull Char enemy) {
		summon();
		return true;
	}

	private void summon() {
		lastPedestal = targetPedestal;

		getSprite().centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.4f, 2 );		
		Sample.INSTANCE.play( Assets.SND_CHALLENGE );
		
		int undeadsToSummon = maxArmySize() - Undead.count;

		Level level = Dungeon.level;

		for (int i=0; i < undeadsToSummon; i++) {
			int pos = level.getEmptyCellNextTo(lastPedestal);

			if (level.cellValid(pos)) {
				Mob servant = new Undead();
				servant.setPos(pos);
				level.spawnMob(servant, 0);

				WandOfBlink.appear(servant, pos);
				new Flare(3, 32).color(0x000000, false).show(servant.getSprite(), 2f);

				Actor.addDelayed(new Pushing(servant, lastPedestal, servant.getPos()), -1);
			}
		}
		yell(Game.getVar(R.string.King_Info2));
	}
	
	@Override
	public void notice() {
		super.notice();
		yell(Game.getVar(R.string.King_Info3));
	}
	
	public static class Undead extends UndeadMob {
		
		public static int count = 0;
		
		public Undead() {
			spriteClass = UndeadSprite.class;
			
			hp(ht(28));
			defenseSkill = 15;
			
			exp = 0;
			
			setState(WANDERING);
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
		public int attackProc(@NonNull Char enemy, int damage ) {
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
