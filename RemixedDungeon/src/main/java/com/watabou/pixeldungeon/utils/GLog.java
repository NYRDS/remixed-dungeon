package com.watabou.pixeldungeon.utils;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.PUtil;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.Util;
import com.watabou.utils.Signal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class GLog {

	private static final String RE_PD_LOG_FILE_LOG = "RePdLogFile.log";

	private static final String TAG = "GAME";

	public static final String POSITIVE		= "++ ";
	public static final String NEGATIVE		= "-- ";
	public static final String WARNING		= "** ";
	public static final String HIGHLIGHT	= "@@ ";

	public static final Signal<String> update = new Signal<>();
	
	// Queue to store recent log messages with timestamps
	private static final int MAX_LOG_MESSAGES = 100;
	private static Queue<TimedLogMessage> recentMessages = new LinkedList<>();
	private static volatile long lastCallTimestamp = 0;
	
	// Inner class to hold log messages with timestamps
	private static class TimedLogMessage {
		public final String message;
		public final long timestamp;
		
		public TimedLogMessage(String message) {
			this.message = message;
			this.timestamp = System.currentTimeMillis();
		}
	}

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
				toFile("log started %s !", GameLoop.version);
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
			PUtil.slog(TAG, text);
		}

		// Add to recent messages queue
		synchronized(recentMessages) {
			recentMessages.offer(new TimedLogMessage(finalText));
			while (recentMessages.size() > MAX_LOG_MESSAGES) {
				recentMessages.poll();
			}
		}

		GameLoop.pushUiTask(() -> update.dispatch(finalText));
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

			PUtil.slog(TAG, text);
		}
	}
	
	/**
	 * Get recent log messages since the last call to this method
	 * @return Array of recent log messages
	 */
	public static String[] getRecentMessagesSinceLastCall() {
		long currentTimestamp = System.currentTimeMillis();
		java.util.List<String> messages = new java.util.ArrayList<>();
		
		synchronized(recentMessages) {
			for (TimedLogMessage timedMsg : recentMessages) {
				if (timedMsg.timestamp >= lastCallTimestamp) {
					messages.add(timedMsg.message);
				}
			}
		}
		
		lastCallTimestamp = currentTimestamp;
		return messages.toArray(new String[0]);
	}
	
	/**
	 * Get all recent log messages
	 * @return Array of recent log messages
	 */
	public static String[] getAllRecentMessages() {
		java.util.List<String> messages = new java.util.ArrayList<>();
		
		synchronized(recentMessages) {
			for (TimedLogMessage timedMsg : recentMessages) {
				messages.add(timedMsg.message);
			}
		}
		
		return messages.toArray(new String[0]);
	}
}
