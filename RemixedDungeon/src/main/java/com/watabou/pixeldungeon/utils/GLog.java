
package com.watabou.pixeldungeon.utils;

import android.util.Log;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.Util;
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

	public static final Signal<String> update = new Signal<>();
	
	private static FileWriter logWriter;
	private static boolean readonlySd = false;

	public static boolean enabled = true;


	public static synchronized void toFile(String text, Object... args) {
		debug(text,args);

		if(readonlySd) {
			return;
		}
		
		if(logWriter==null) {
			File logFile = FileSystem.getExternalStorageFile(RE_PD_LOG_FILE_LOG);
			
			if(logFile.length() > 1024*1024) {
				try {
					logFile.delete();
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
		
		text = today + "\t" + text + "\n";
		
		try {
			logWriter.write(text);
			logWriter.flush();
		} catch (IOException e) {
			readonlySd = true;
		}
	}

	private static void glog( String text, Object... args ) {
		if(!enabled) {
			return;
		}

		if (args.length > 0) {
			text = Utils.format( text, args );
		}

		if(text.isEmpty()) {
			return;
		}
		final String finalText = text;

		if(Util.isDebug()) {
			Log.i(TAG, text);
		}

		GameLoop.pushUiTask(() -> update.dispatch(finalText)
		);
	}

	public static void i( String text, Object... args ) {
		glog(StringsManager.maybeId(text), args);
	}
	
	public static void p( String text, Object... args ) {
		glog( POSITIVE + StringsManager.maybeId(text), args );
	}
	
	public static void n( String text, Object... args ) {
		glog( NEGATIVE + StringsManager.maybeId(text), args );
	}
	
	public static void w( String text, Object... args ) {
		glog( WARNING + StringsManager.maybeId(text), args );
	}
	
	public static void h( String text, Object... args ) {
		glog( HIGHLIGHT + StringsManager.maybeId(text), args );
	}

	public static void debug( String text, Object... args ) {
		if(Util.isDebug()) {
			if (args.length > 0) {
				text = Utils.format( text, args );
			}

			if(text.isEmpty()) {
				return;
			}

			Log.i(TAG, text);
		}
	}


}
