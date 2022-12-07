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
package com.watabou.pixeldungeon.utils;

import android.util.Log;

import com.nyrds.android.util.FileSystem;
import com.nyrds.retrodungeon.ml.BuildConfig;
import com.watabou.noosa.Game;
import com.watabou.utils.Signal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class GLog {

	private static final String RE_PD_LOG_FILE_LOG = "RePdLogFile.log";

	private static final String TAG = "GAME";
	
	public static final String POSITIVE		= "++ ";
	public static final String NEGATIVE		= "-- ";
	public static final String WARNING		= "** ";
	public static final String HIGHLIGHT	= "@@ ";

	public static Signal<String> update = new Signal<>();
	
	private static FileWriter logWriter;
	private static boolean readonlySd = false;


	public static synchronized void toFile(String text, Object... args) {
		if(readonlySd) {
			return;
		}
		
		if(logWriter==null) {
			File logFile = FileSystem.getExternalStorageFile(RE_PD_LOG_FILE_LOG);
			
			if(logFile.length() > 1024*1024) {
				try {
					logFile.createNewFile();
				} catch (IOException e) {
					readonlySd = true;
					return;
				}
			}
			
			try {
				logWriter = new FileWriter(logFile,true);
				toFile("log started %s !", Game.version);
			} catch (Exception e) {
				readonlySd = true;
				return;
			}
		}
		
		Date today = Calendar.getInstance().getTime(); 
		
		if (args.length > 0) {
			text = Utils.format( text, args );
		}
		
		text = today.toString() + "\t" + text + "\n";
		
		try {
			logWriter.write(text);
			logWriter.flush();
		} catch (IOException e) {
			readonlySd = true;
		}
	}
	
	public static void i( String text, Object... args ) {
		
		if (args.length > 0) {
			text = Utils.format( text, args );
		}

		if(text.isEmpty()) {
			return;
		}

		if(BuildConfig.DEBUG) {
			Log.i(TAG, text);
		}

		if(!Game.isPaused()) {
			final String finalText = text;
			Game.executeInGlThread(new Runnable() {
				                       @Override
				                       public void run() {
					                       update.dispatch(finalText);
				                       }
			                       }
			);
		}
	}
	
	public static void p( String text, Object... args ) {
		i( POSITIVE + text, args );
	}
	
	public static void n( String text, Object... args ) {
		i( NEGATIVE + text, args );
	}
	
	public static void w( String text, Object... args ) {
		i( WARNING + text, args );
	}
	
	public static void h( String text, Object... args ) {
		i( HIGHLIGHT + text, args );
	}

}
