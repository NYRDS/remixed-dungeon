package com.nyrds.platform;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by mike on 09.03.2016.
 */
public class EventCollector {

	static private FirebaseAnalytics mFirebaseAnalytics;
	static private boolean mDisabled = true;

	private static boolean analyticsUsable() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_COLLECT_STATS,1) > 0;
	}

	static public void init() {
	    if(analyticsUsable() && !Util.isDebug()) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(RemixedDungeonApp.getContext());
            mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
            mDisabled = false;
        }
	}

	static public void logCountedEvent(String event, int threshold) {
		final String key = "CountedEvent_"+event;
		int count = Preferences.INSTANCE.getInt(key,0);
		count++;

		if(count==threshold) {
			logEvent(event);
		}
		Preferences.INSTANCE.put(key, count);
	}

	static public void logEvent(String event) {
		if (!mDisabled) {
			FirebaseCrashlytics.getInstance().log(event);

			Bundle params = new Bundle();
			mFirebaseAnalytics.logEvent(event, params);
		}
	}

	static public void logEvent(String event, double value) {
		if (!mDisabled) {
			FirebaseCrashlytics.getInstance().log(event);

			Bundle params = new Bundle();
			params.putDouble("dv", value);
			mFirebaseAnalytics.logEvent(event, params);
		}
	}

	static public void logEvent(String category, String event) {
		if (!mDisabled) {
			FirebaseCrashlytics.getInstance().log(category+":"+event);

			Bundle params = new Bundle();
			params.putString("event", event);
			mFirebaseAnalytics.logEvent(category, params);
		}
	}

	static public void levelUp(String character, long level) {
		if(!mDisabled && ModdingMode.inRemixed()) {
			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.CHARACTER, character);
			bundle.putLong(FirebaseAnalytics.Param.LEVEL, level);
			mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
		}
	}

	static public void badgeUnlocked(String badgeId) {
		if(!mDisabled && ModdingMode.inRemixed()) {
			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, badgeId);
			mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
		}
	}

	static public void logEvent(String category, Map<String,String> eventData) {
		if (!mDisabled) {

			Bundle params = new Bundle();

			for (String key:eventData.keySet()) {
				params.putString(key, eventData.get(key));
			}

			mFirebaseAnalytics.logEvent(category, params);
		}
	}

	static public void logEvent(String category, String event, String label) {
		if (!mDisabled) {
			FirebaseCrashlytics.getInstance().log(category+":"+event+":"+label);

			Bundle params = new Bundle();
			params.putString("event", event);
			params.putString("label", label);
			mFirebaseAnalytics.logEvent(category, params);
		}
	}

	static public void logScene(final String scene) {
		if (!mDisabled) {
			GameLoop.runOnMainThread(() -> mFirebaseAnalytics.setCurrentScreen(Game.instance(), scene, null));
		}
	}

	static public void logException() {
		logException(new Exception(),1);
	}

	static public void logException(String desc) {
		logException(new Exception(desc),1);
	}

	static private void logException(Throwable e, int level) {
		if(!mDisabled) {
			StackTraceElement [] stackTraceElements = e.getStackTrace();
			e.setStackTrace(Arrays.copyOfRange(stackTraceElements,level,stackTraceElements.length));
			FirebaseCrashlytics.getInstance().recordException(e);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
			ps.close();

			FirebaseCrashlytics.getInstance().log(Utils.EMPTY_STRING+System.currentTimeMillis()+":"+e.getMessage() + ":"+ baos.toString());

			GLog.toFile(baos.toString());

			if(Util.isDebug()) {
				throw new RuntimeException(new Exception(e));
			}
		}
	}

	static public void logException(Throwable e) {
		logException(e,0);
	}

	static public void logException(Throwable e, String desc) {
		if(!mDisabled) {
			FirebaseCrashlytics.getInstance().log(desc);
			logException(e, 0);
		}
	}

	public static void setSessionData(String key, boolean value) {
		if(!mDisabled) {
			FirebaseCrashlytics.getInstance().setCustomKey(key, value);
		}
	}

	public static void setSessionData(String key, String value) {
		if(!mDisabled) {
			FirebaseCrashlytics.getInstance().setCustomKey(key, value);
		}
	}

    public static void disable() {
		mDisabled = true;
    }
}
