package com.nyrds.retrodungeon.mobs.npc;

import com.nyrds.retrodungeon.items.artifacts.CandleOfMindVision;
import com.nyrds.retrodungeon.items.icecaves.IceKey;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.HashSet;
import java.util.Set;

public class CagedKobold extends ImmortalNPC {

	private static final String TXT_QUEST_START = Game.getVar(R.string.CagedKobold_Intro);
	private static final String TXT_QUEST_END = Game.getVar(R.string.CagedKobold_Quest_End);
	private static final String TXT_MESSAGE1 = Game.getVar(R.string.CagedKobold_Message1);
	private static final String TXT_MESSAGE2 = Game.getVar(R.string.CagedKobold_Message2);
	private static final String TXT_MESSAGE3 = Game.getVar(R.string.CagedKobold_Message3);

	private static boolean spawned;

	private static String[] TXT_PHRASES = {TXT_MESSAGE1, TXT_MESSAGE2, TXT_MESSAGE3};

	public CagedKobold() {
	}
	

	public static void spawn( RegularLevel level, Room room ) {
		if (!spawned) {
			CagedKobold npc = new CagedKobold();
			int cell;
			do {
				cell = room.random(level);
			} while (level.map[cell] == Terrain.EXIT);
			npc.setPos(cell);
			level.spawnMob(npc);
			spawned = true;
		}
	}

	@Override
	protected boolean act() {

		throwItem();

		getSprite().turnTo( getPos(), Dungeon.hero.getPos() );
		spend( TICK );
		return true;
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );
		if(Quest.completed) {
			return true;
		}
		if (Quest.given) {
			
			Item item = hero.belongings.getItem( IceKey.class );
			if (item != null) {

				item.removeItemFrom(Dungeon.hero);

				Item reward = new CandleOfMindVision();

				if (reward.doPickUp( Dungeon.hero )) {
					GLog.i( Hero.TXT_YOU_NOW_HAVE, reward.name() );
				} else {
					Dungeon.level.drop(reward, hero.getPos()).sprite.drop();
				}
				Quest.complete();
				GameScene.show( new WndQuest( this, TXT_QUEST_END ) );

				CellEmitter.get( getPos() ).start( Speck.factory( Speck.LIGHT ), 0.2f, 3 );
				this.die(null);
			} else {
				int index = Random.Int(0, TXT_PHRASES.length);
				say(TXT_PHRASES[index]);
			}
			
		} else {
			GameScene.show( new WndQuest( this, TXT_QUEST_START ) );
			Quest.given = true;
			Quest.process();
			Journal.add( Journal.Feature.CAGEDKOBOLD.desc() );
		}
		return true;
	}
		
	private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();
	static {
		IMMUNITIES.add( Paralysis.class );
		IMMUNITIES.add( Roots.class );
	}
	
	@Override
	public Set<Class<?>> immunities() {
		return IMMUNITIES;
	}
	
	public static class Quest {

		private static boolean completed;
		private static boolean given;
		private static boolean processed;

		private static int depth;

		public static void reset() {
			completed = false;
			processed = false;
			given = false;
		}

		private static final String COMPLETED   = "completed";
		private static final String NODE		= "cagedkobold";
		private static final String GIVEN		= "given";
		private static final String PROCESSED	= "processed";
		private static final String DEPTH		= "depth";
		private static final String SPAWNED		= "spawned";

		public static void storeInBundle( Bundle bundle ) {
			Bundle node = new Bundle();

			node.put(GIVEN, given);
			node.put(DEPTH, depth);
			node.put(PROCESSED, processed);
			node.put(COMPLETED, completed);
			node.put(SPAWNED, spawned);

			bundle.put( NODE, node );
		}
		
		public static void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );

			if (!node.isNull() ) {
				given	= node.getBoolean( GIVEN );
				depth	= node.getInt( DEPTH );
				processed	= node.getBoolean( PROCESSED );
				completed = node.getBoolean( COMPLETED );
				spawned = node.getBoolean( SPAWNED );
			}
		}

		public static void process() {
			if (given && !processed) {
				processed = true;
			}
		}

		public static void complete() {
			completed = true;
			Journal.remove( Journal.Feature.CAGEDKOBOLD.desc() );
		}
	}
}


