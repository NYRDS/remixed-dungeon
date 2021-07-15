package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.utils.Bundle;

public class CagedKobold extends ImmortalNPC {

	private static boolean spawned;

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
    public boolean act() {

		ItemUtils.throwItemAway(getPos());

		getSprite().turnTo( getPos(), Dungeon.hero.getPos() );
		spend( TICK );
		return true;
	}

	@Override
	public boolean interact(final Char hero) {
		getSprite().turnTo( getPos(), hero.getPos() );
		if(Quest.completed) {
			return true;
		}
		if (Quest.given) {

			if (exchangeItem(hero,"IceKey", "CandleOfMindVision")) {
				Quest.complete();
                GameScene.show( new WndQuest( this, StringsManager.getVar(R.string.CagedKobold_Quest_End)) );

				CellEmitter.get( getPos() ).start( Speck.factory( Speck.LIGHT ), 0.2f, 3 );
				getSprite().killAndErase();
				destroy();
			} else {
				sayRandomPhrase(R.string.CagedKobold_Message1,
								R.string.CagedKobold_Message2,
								R.string.CagedKobold_Message3);
			}
			
		} else {
            GameScene.show( new WndQuest( this, StringsManager.getVar(R.string.CagedKobold_Intro)) );
			Quest.given = true;
			Quest.process();
			Journal.add( Journal.Feature.CAGEDKOBOLD.desc() );
		}
		return true;
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


