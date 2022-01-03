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

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndSadGhostNecro;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.quest.DriedRose;
import com.watabou.pixeldungeon.items.quest.RatSkull;
import com.watabou.pixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.SewerLevel;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.FetidRatSprite;
import com.watabou.pixeldungeon.sprites.GhostSprite;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.pixeldungeon.windows.WndSadGhost;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Ghost extends NPC {

	{
		spriteClass = GhostSprite.class;
		
		flying = true;
		
		setState(MobAi.getStateByClass(Wandering.class));
	}

	@Packable
	private boolean persuade = false;

	@Packable
	private boolean introduced = false;
	private WndSadGhostNecro window;

	public Ghost() {
		addImmunity( Paralysis.class );
		addImmunity( Stun.class );
		addImmunity( Roots.class );
	}

	@Override
	public boolean act() {

		if(Quest.given && Math.random() < 0.1) {
			setTarget(Dungeon.hero.getControlTarget().getPos());
		}

		return super.act();
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return 1000;
	}
	
	@Override
	public String defenseVerb() {
        return StringsManager.getVar(R.string.Ghost_Defense);
    }
	
	@Override
	public float speed() {
		return 0.5f;
	}
	

	@Override
	public void damage(int dmg, @NotNull NamedEntityKind src ) {
	}
	
	@Override
	public boolean add(Buff buff ) {
        return false;
    }
	
	@Override
	public boolean reset() {
		return true;
	}
	
	@Override
	public boolean interact(final Char hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

		if (hero.getHeroClass()==HeroClass.NECROMANCER){
			if (!introduced){
				window = new WndSadGhostNecro();
				GameScene.show( window );
				introduced = true;
				return true;
			}
			else {
				if (window != null){
					persuade = window.getPersuade();
				}
			}
		}

		Sample.INSTANCE.play( Assets.SND_GHOST );
		
		if (persuade || Quest.given ) {
			Item item = Quest.alternative ?
				hero.getBelongings().getItem( RatSkull.class ) :
				hero.getBelongings().getItem( DriedRose.class );
			if(persuade){
				item = Quest.alternative ?
					new RatSkull() :
					new DriedRose();
			}
			if (persuade || item != null) {
				GameScene.show( new WndSadGhost( this, item ) );
			} else {
                GameScene.show( new WndQuest( this, Quest.alternative ? StringsManager.getVar(R.string.Ghost_Rat2) : StringsManager.getVar(R.string.Ghost_Rose2)) );
				
				int newPos = -1;
				for (int i=0; i < 10; i++) {
					newPos = Dungeon.level.randomRespawnCell();
					if (newPos != -1) {
						break;
					}
				}
				if (newPos != -1) {
					
					Actor.freeCell( getPos() );
					
					CellEmitter.get( getPos() ).start( Speck.factory( Speck.LIGHT ), 0.2f, 3 );
					setPos(newPos);
					getSprite().place( getPos() );
					getSprite().setVisible(Dungeon.isCellVisible(getPos()));
				}
			}
			
		} else {
            GameScene.show( new WndQuest( this, Quest.alternative ? StringsManager.getVar(R.string.Ghost_Rat1) : StringsManager.getVar(R.string.Ghost_Rose1)) );
			Quest.given = true;
			
			Journal.add( Journal.Feature.GHOST.desc() );
		}
		return true;
	}

	public static class Quest {

		private static boolean spawned;
		private static boolean alternative;
		private static boolean given;
		private static boolean processed;

		private static int depth;
		private static int left2kill;
		
		private static Item weapon;
		private static Item armor;
		
		public static void reset() {
			spawned = false;
			weapon = null;
			armor = null;
		}
		
		private static final String NODE		= "sadGhost";
		
		private static final String SPAWNED		= "spawned";
		private static final String ALTERNATIVE	= "alternative";
		private static final String LEFT2KILL	= "left2kill";
		private static final String GIVEN		= "given";
		private static final String PROCESSED	= "processed";
		private static final String DEPTH		= "depth";
		private static final String WEAPON		= "weapon";
		private static final String ARMOR		= "armor";
		
		public static void storeInBundle( Bundle bundle ) {
			
			Bundle node = new Bundle();
			
			node.put( SPAWNED, spawned );
			
			if (spawned) {
				
				node.put( ALTERNATIVE, alternative );
				if (!alternative) {
					node.put( LEFT2KILL, left2kill );
				}
				
				node.put( GIVEN, given );
				node.put( DEPTH, depth );
				node.put( PROCESSED, processed );
				
				node.put( WEAPON, weapon );
				node.put( ARMOR, armor );
			}
			
			bundle.put( NODE, node );
		}
		
		public static void restoreFromBundle( Bundle bundle ) {
			
			Bundle node = bundle.getBundle( NODE );
			
			if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {
				
				alternative	=  node.getBoolean( ALTERNATIVE );
				if (!alternative) {
					left2kill = node.getInt( LEFT2KILL );
				}
				
				given	= node.getBoolean( GIVEN );
				depth	= node.getInt( DEPTH );
				processed	= node.getBoolean( PROCESSED );
				
				weapon	= (Item)node.get( WEAPON );
				armor	= (Item)node.get( ARMOR );
			} else {
				reset();
			}
		}
		
		public static void spawn( SewerLevel level ) {
			if (!spawned && Dungeon.depth > 1 && Random.Int( 5 - Dungeon.depth ) == 0) {
				
				Ghost ghost = new Ghost();
				do {
					ghost.setPos(level.randomRespawnCell());
				} while (ghost.getPos() == -1);
				level.mobs.add( ghost );
				Actor.occupyCell( ghost );
				
				spawned = true;
				alternative = Random.Int( 2 ) == 0;
				if (!alternative) {
					left2kill = 8;
				}
				
				given = false;
				processed = false;
				depth = Dungeon.depth;

				makeReward();
			}
		}

		private static void makeReward() {
			do {
				weapon = Treasury.getLevelTreasury().bestOf(Treasury.Category.WEAPON, 4 );
			} while (weapon instanceof MissileWeapon);

			armor = Treasury.getLevelTreasury().bestOf(Treasury.Category.ARMOR,4 );

			weapon.identify();
			armor.identify();
		}

		public static void process( int pos ) {
			if (spawned && given && !processed && (depth == Dungeon.depth)) {
				if (alternative) {
					Level level = Dungeon.level;
					FetidRat rat = new FetidRat();
					int ratPos = rat.respawnCell(level);
					if (level.cellValid(ratPos)) {
						rat.setPos(ratPos);
						level.spawnMob(rat);
						processed = true;
					}
					
				} else {
					
					if (Random.Int( left2kill ) == 0) {
						Dungeon.level.animatedDrop( new DriedRose(), pos );
						processed = true;
					} else {
						left2kill--;
					}
					
				}
			}
		}
		
		public static void complete() {
			weapon = null;
			armor = null;
			
			Journal.remove( Journal.Feature.GHOST.desc() );
		}

		public static Item getWeapon() {
			if(weapon==null) {
				EventCollector.logException("null weapon");
				makeReward();
			}
			return weapon;
		}


		public static Item getArmor() {
			if(armor==null) {
				EventCollector.logException("null armor");
				makeReward();
			}
			return armor;
		}

	}
	
	public static class FetidRat extends Mob {

		public FetidRat() {
			spriteClass = FetidRatSprite.class;
			
			hp(ht(15));
			baseDefenseSkill = 5;
			baseAttackSkill  = 12;
			dmgMin = 2;
			dmgMax = 6;
			dr = 2;
			
			exp = 0;
			
			setState(MobAi.getStateByClass(Wandering.class));

			collect( new RatSkull() );
			addImmunity( Paralysis.class );
		}

		@Override
		public int defenseProc( Char enemy, int damage ) {
			GameScene.add( Blob.seed( getPos(), 20, ParalyticGas.class ) );
			return super.defenseProc(enemy, damage);
		}
	}
}
