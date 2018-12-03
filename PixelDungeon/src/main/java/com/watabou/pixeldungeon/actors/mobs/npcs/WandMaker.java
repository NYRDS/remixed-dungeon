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

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.potions.PotionOfStrength;
import com.watabou.pixeldungeon.items.quest.CorpseDust;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.wands.WandOfAmok;
import com.watabou.pixeldungeon.items.wands.WandOfAvalanche;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.items.wands.WandOfDisintegration;
import com.watabou.pixeldungeon.items.wands.WandOfFirebolt;
import com.watabou.pixeldungeon.items.wands.WandOfLightning;
import com.watabou.pixeldungeon.items.wands.WandOfPoison;
import com.watabou.pixeldungeon.items.wands.WandOfRegrowth;
import com.watabou.pixeldungeon.items.wands.WandOfSlowness;
import com.watabou.pixeldungeon.items.wands.WandOfTelekinesis;
import com.watabou.pixeldungeon.levels.PrisonLevel;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.sprites.WandmakerSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.pixeldungeon.windows.WndWandmaker;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class WandMaker extends NPC {

	{	
		spriteClass = WandmakerSprite.class;
	}

	@Override
	protected boolean act() {
		throwItem();
		return super.act();
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		return 1000;
	}
		
	@Override
	public void damage( int dmg, Object src ) {
	}
	
	@Override
	public void add( Buff buff ) {
	}
	
	@Override
	public boolean reset() {
		return true;
	}
	
	@Override
	public boolean interact(final Hero hero) {
		
		getSprite().turnTo( getPos(), hero.getPos() );
		if (Quest.given) {
			
			Item item = Quest.alternative ?
				hero.belongings.getItem( CorpseDust.class ) :
				hero.belongings.getItem( Rotberry.Seed.class );
			if (item != null) {
				GameScene.show( new WndWandmaker( this, item ) );
			} else {
				tell( Quest.alternative ? Game.getVar(R.string.WandMaker_Dust2) : Game.getVar(R.string.WandMaker_Berry2), hero.className() );
			}
			
		} else {
			tell( Quest.alternative ? Game.getVar(R.string.WandMaker_Dust1) : Game.getVar(R.string.WandMaker_Berry1) );
			Quest.given = true;
			
			Quest.placeItem();
			
			Journal.add( Journal.Feature.WANDMAKER.desc() );
		}
		
		return true;
	}
	
	private void tell( String format, Object...args ) {
		GameScene.show( new WndQuest( this, Utils.format( format, args ) ) );
	}
	
	public static Wand makeBattleWand() {
		Wand wand = null;
		switch (Random.Int( 5 )) {
		case 0:
			wand = new WandOfAvalanche();
			break;
		case 1:
			wand = new WandOfDisintegration();
			break;
		case 2:
			wand = new WandOfFirebolt();
			break;
		case 3:
			wand = new WandOfLightning();
			break;
		case 4:
			wand = new WandOfPoison();
			break;
		}
		wand.random().upgrade();
		
		return wand;
	}
	
	public static Wand makeNonBattleWand() {
		Wand wand = null;
		
		switch (Random.Int( 5 )) {
		case 0:
			wand = new WandOfAmok();
			break;
		case 1:
			wand = new WandOfBlink();
			break;
		case 2:
			wand = new WandOfRegrowth();
			break;
		case 3:
			wand = new WandOfSlowness();
			break;
		case 4:
			wand = new WandOfTelekinesis();
			break;
		}
		wand.random().upgrade();
		return wand;
	}
	
	public static class Quest {
		
		private static boolean spawned;
		
		private static boolean alternative;
		
		private static boolean given;
		
		public static void reset() {
			spawned = false;
		}
		
		private static final String NODE		= "wandmaker";
		
		private static final String SPAWNED		= "spawned";
		private static final String ALTERNATIVE	= "alternative";
		private static final String GIVEN		= "given";
		
		public static void storeInBundle( Bundle bundle ) {
			
			Bundle node = new Bundle();
			
			node.put( SPAWNED, spawned );
			
			if (spawned) {
				
				node.put( ALTERNATIVE, alternative );
				
				node.put(GIVEN, given );
				
			}
			
			bundle.put( NODE, node );
		}
		
		public static void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );
			
			if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {
				
				alternative	=  node.getBoolean( ALTERNATIVE );
				given = node.getBoolean( GIVEN );
			} else {
				reset();
			}
		}
		
		public static void spawn( PrisonLevel level, Room room ) {
			if(room == null) {
				EventCollector.logException("spawn in null room");
				return;
			}
			
			if (!spawned && Dungeon.depth > 6 && Random.Int( 10 - Dungeon.depth ) == 0) {
				
				WandMaker npc = new WandMaker();
				do {
					int cell = room.random(level);
					npc.setPos(cell);
				} while (level.map[npc.getPos()] == Terrain.ENTRANCE);
				level.mobs.add( npc );
				Actor.occupyCell( npc );
				
				spawned = true;
				alternative = Random.Int( 2 ) == 0;
				
				given = false;
			}
		}
		
		public static void placeItem() {
			if (alternative) {
				
				ArrayList<Heap> candidates = new ArrayList<>();
				for (Heap heap : Dungeon.level.allHeaps()) {
					if (heap.type == Heap.Type.SKELETON && !Dungeon.visible[heap.pos]) {
						candidates.add( heap );
					}
				}
				
				if (candidates.size() > 0) {
					Random.element( candidates ).drop( new CorpseDust() );
				} else {
					int pos = Dungeon.level.randomRespawnCell();
					while (Dungeon.level.getHeap( pos ) != null) {
						pos = Dungeon.level.randomRespawnCell();
					}
					
					Heap heap = Dungeon.level.drop( new CorpseDust(), pos );
					heap.type = Heap.Type.SKELETON;
					heap.sprite.link();
				}
				
			} else {
				
				int shrubPos = Dungeon.level.randomRespawnCell();
				while (Dungeon.level.getHeap( shrubPos ) != null) {
					shrubPos = Dungeon.level.randomRespawnCell();
				}
				Dungeon.level.plant( new Rotberry.Seed(), shrubPos );
				
			}
		}
		
		public static void complete() {			
			Journal.remove( Journal.Feature.WANDMAKER.desc() );
		}
	}
	
	public static class Rotberry extends Plant {
		
		{
			image = 7;
			plantName = Game.getVar(R.string.WandMaker_RotberryName);
		}
		
		@Override
		public void activate( Char ch ) {
			super.activate( ch );
			
			GameScene.add( Blob.seed( pos, 100, ToxicGas.class ) );
			
			Dungeon.level.drop( new Seed(), pos ).sprite.drop();
			
			if (ch != null) {
				Buff.prolong( ch, Roots.class, TICK * 3 );
			}
		}
		
		@Override
		public String desc() {
			return Game.getVar(R.string.WandMaker_RotberryDesc);
		}
		
		public static class Seed extends Plant.Seed {
			{
				plantName = Game.getVar(R.string.WandMaker_RotberryName);
				
				name = Utils.format(Game.getVar(R.string.Plant_Seed), plantName);
				image = ItemSpriteSheet.SEED_ROTBERRY;
				
				plantClass = Rotberry.class;
				alchemyClass = PotionOfStrength.class;
			}
			
			@Override
			public void execute( Hero hero, String action ) {
				
				super.execute( hero, action );
				
				if (action.equals( Food.AC_EAT )) {
					GameScene.add( Blob.seed( hero.getPos(), 100, ToxicGas.class ) );
					GameScene.add( Blob.seed( hero.getPos(), 100, ParalyticGas.class ) );					
				}
			}

			@Override
			public Item burn(int cell) {
				return this;
			}

			@Override
			public boolean collect( Bag container ) {
				if (super.collect( container )) {

					Dungeon.challengeAllMobs(Dungeon.hero,Assets.SND_CHALLENGE);
					
					GLog.w(Game.getVar(R.string.WandMaker_RotberryInfo));
					return true;
				} else {
					return false;
				}
			}
			
			@Override
			public String desc() {
				return Game.getVar(R.string.WandMaker_RotberryDesc);
			}
		}
	}
}
