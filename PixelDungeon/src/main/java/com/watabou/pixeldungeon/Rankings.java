/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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
package com.watabou.pixeldungeon;

import com.nyrds.Packable;
import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.SystemTime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public enum Rankings {
	
	INSTANCE;
	
	public static final int TABLE_SIZE	= 101;

	public static final String RANKINGS_FILE = "rankings.dat";
	public static final String DETAILS_FILE  = "game_%d.dat";
	
	public ArrayList<Record> records;
	public int lastRecord;
	public int totalNumber;
	public int wonNumber;
	public int happyWonNumber;
	
	public enum gameOver{ LOSE, WIN_AMULET, WIN_HAPPY }

	public void submit( gameOver winLevel, String resultDescription ) {
		
		load();

		Hero hero = Dungeon.hero;

		Record rec = new Record();
		
		rec.info	    = resultDescription;
		rec.win		    = winLevel  != gameOver.LOSE;
		rec.heroClass	= hero.heroClass;
		rec.score	    = score(winLevel);
		rec.mod			= PixelDungeon.activeMod();
		rec.gameId      = Dungeon.gameId;


		Map<String,String> resDesc = new HashMap<>();
		resDesc.put("class",     hero.heroClass.toString());
		resDesc.put("subclass",  Dungeon.hero.subClass.toString());

		resDesc.put("resDesc",  resultDescription);
		resDesc.put("mod",      rec.mod);
		resDesc.put("duration", Float.toString(Statistics.duration));

		EventCollector.logEvent("gameover", resDesc);

		if (!ModdingMode.inMod()){
			Game.instance().playGames.submitScores(Game.getDifficulty(), rec.score);
		}

		String gameFile = Utils.format( DETAILS_FILE, SystemTime.now() );
		try {
			Dungeon.saveGame(gameFile);
			rec.gameFile = gameFile;
		} catch (IOException e) {
			rec.gameFile = "";
			EventCollector.logException(e);
		}

		int updateIndex = -1;
		for (int i = 0;i<records.size();++i) {
			Record r = records.get(i);
			if(rec.gameId.equals(r.gameId)) {
				updateIndex = i;
			}
		}

		if(updateIndex >= 0) {
			records.set(updateIndex,rec);
		} else {
			records.add(rec);
		}

		sortAndSave(winLevel, rec);
	}

	private void sortAndSave(gameOver winLevel, Record rec) {
		Collections.sort( records, scoreComparator );

		lastRecord = records.indexOf( rec );

		int size = records.size();
		if (size > TABLE_SIZE) {
			Record removedGame;
			if (lastRecord == size - 1) {
				removedGame = records.remove( size - 2 );
				lastRecord--;
			} else {
				removedGame = records.remove( size - 1 );
			}

			if (removedGame.gameFile.length() > 0) {
				Game.instance().deleteFile( removedGame.gameFile );
			}
		}

		totalNumber++;

		if (winLevel != gameOver.LOSE) {
			wonNumber++;
		}

		if (winLevel == gameOver.WIN_HAPPY) {
			happyWonNumber++;
		}

		Badges.validateGamesPlayed();

		save();
	}

	private int score(gameOver win ) {

		double winC        = Math.pow(1.4f, win.ordinal());
		double challengesC = Math.pow(1.3f, Integer.bitCount(Dungeon.challenges));
		double difficultyC = Math.pow(1.4f, Game.getDifficulty());

		return (int) (difficultyC * challengesC * winC * ( Statistics.goldCollected
									+ (Dungeon.hero.lvl() * Statistics.deepestFloor * 100)
									- Statistics.duration
									+ (Statistics.enemiesSlain * 25)
									+ (Statistics.foodEaten * 111)
									+ (Statistics.piranhasKilled * 666)));
	}
	
	private static final String RECORDS	= "records";
	private static final String LATEST	= "latest";
	private static final String TOTAL	= "total";
	private static final String WON		= "won";
	private static final String HAPPY   = "happy";
	
	public void save() {
		Bundle bundle = new Bundle();
		bundle.put( RECORDS, records );
		bundle.put( LATEST,  lastRecord );
		bundle.put( TOTAL,   totalNumber );
		bundle.put( WON,     wonNumber );
		bundle.put( HAPPY,   happyWonNumber);
		
		try {
			OutputStream output = FileSystem.getOutputStream(RANKINGS_FILE);
			Bundle.write( bundle, output );
			output.close();
		} catch (Exception e) {
			EventCollector.logException(e);
		}
	}
	
	public void load() {
		
		if (records != null) {
			return;
		}

		records = new ArrayList<>();

		if(!FileSystem.getInternalStorageFile(RANKINGS_FILE).exists()) {
			return;
		}

		try {
			InputStream input = FileSystem.getInputStream(RANKINGS_FILE);
			Bundle bundle = Bundle.read( input );
			input.close();

			records.addAll(bundle.getCollection(RECORDS, Record.class));
			
			lastRecord     = bundle.getInt( LATEST );
			totalNumber    = bundle.getInt( TOTAL );
			wonNumber      = bundle.getInt( WON );
			happyWonNumber = bundle.getInt( HAPPY );
			
		} catch (Exception e) {
			EventCollector.logException(e);
		}
	}
	
	public static class Record implements Bundlable {
		
		private static final String REASON	= "reason";
		private static final String WIN		= "win";
		private static final String SCORE	= "score";
		private static final String GAME	= "gameFile";
		private static final String MOD		= "mod";
		
		public String info;
		public boolean win;
		
		public HeroClass heroClass;

		public int score;
		
		public String gameFile;
		public String mod;

		@Packable(defaultValue = Utils.UNKNOWN)
		public String gameId;

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			
			info	= bundle.getString  ( REASON );
			win		= bundle.getBoolean ( WIN    );
			score	= bundle.getInt     ( SCORE  );
			
			heroClass	= HeroClass.restoreFromBundle(bundle);

			gameFile	= bundle.getString( GAME );
			mod			= bundle.optString(MOD, ModdingMode.REMIXED);
		}
		
		@Override
		public void storeInBundle( Bundle bundle ) {
			
			bundle.put( REASON, info );
			bundle.put( WIN,   win );
			bundle.put( SCORE, score );
			
			heroClass.storeInBundle( bundle );

			bundle.put( GAME, gameFile );
			bundle.put( MOD, mod );
		}
		
		public boolean dontPack() {
			return false;
		}
	}

	private static final Comparator<Record> scoreComparator = new Comparator<Rankings.Record>() {
		@Override
		public int compare( Record lhs, Record rhs ) {
			return (int)Math.signum( rhs.score - lhs.score );
		}
	};
}
