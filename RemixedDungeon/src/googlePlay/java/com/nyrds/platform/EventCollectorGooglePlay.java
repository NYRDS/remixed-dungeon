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
import com.nyrds.util.Utils;
import com.nyrds.util.events.IEventCollector;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by mike on 09.03.2016.
 */
public class EventCollectorGooglePlay implements IEventCollector {

	private FirebaseAnalytics mFirebaseAnalytics;

	private boolean analyticsUsable() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_COLLECT_STATS,1) > 0;
	}

	public EventCollectorGooglePlay() {
	    if(analyticsUsable()) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(RemixedDungeonApp.getContext());
            mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
        }
	}

	public void logCountedEvent(String event, int threshold) {
		final String key = "CountedEvent_"+event;
		int count = Preferences.INSTANCE.getInt(key,0);
		count++;

		if(count==threshold) {
			logEvent(event);
		}
		Preferences.INSTANCE.put(key, count);
	}

	public void logEvent(String event) {
		FirebaseCrashlytics.getInstance().log(event);

		Bundle params = new Bundle();
		mFirebaseAnalytics.logEvent(event, params);
	}

	public void logEvent(String event, double value) {
		FirebaseCrashlytics.getInstance().log(event);

		Bundle params = new Bundle();
		params.putDouble("dv", value);
		mFirebaseAnalytics.logEvent(event, params);
	}

	public void logEvent(String category, String event) {
		FirebaseCrashlytics.getInstance().log(category+":"+event);

		Bundle params = new Bundle();
		params.putString("event", event);
		mFirebaseAnalytics.logEvent(category, params);
	}

	public void levelUp(String character, long level) {
		if(ModdingMode.inRemixed()) {
			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.CHARACTER, character);
			bundle.putLong(FirebaseAnalytics.Param.LEVEL, level);
			mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
		}
	}

	public void badgeUnlocked(String badgeId) {
		if(ModdingMode.inRemixed()) {
			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, badgeId);
			mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
		}
	}

	public void logEvent(String category, Map<String,String> eventData) {
			Bundle params = new Bundle();

			for (String key:eventData.keySet()) {
				params.putString(key, eventData.get(key));
			}

			mFirebaseAnalytics.logEvent(category, params);
	}

	public void logEvent(String category, String event, String label) {
			FirebaseCrashlytics.getInstance().log(category+":"+event+":"+label);

			Bundle params = new Bundle();
			params.putString("event", event);
			params.putString("label", label);
			mFirebaseAnalytics.logEvent(category, params);
	}

	public void logScene(final String scene) {
		GameLoop.runOnMainThread(() -> mFirebaseAnalytics.setCurrentScreen(Game.instance(), scene, null));
	}

	public void logException(Throwable e, int level) {

		StackTraceElement [] stackTraceElements = e.getStackTrace();
		e.setStackTrace(Arrays.copyOfRange(stackTraceElements,level,stackTraceElements.length));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		ps.close();
		GLog.toFile(baos.toString());

		FirebaseCrashlytics.getInstance().recordException(e);
		FirebaseCrashlytics.getInstance().log(Utils.EMPTY_STRING+System.currentTimeMillis()+":"+e.getMessage() + ":"+ baos.toString());
	}

	public void logException(Throwable e, String desc) {
		logException(e, 0);
		FirebaseCrashlytics.getInstance().log(desc);
	}

	public void setSessionData(String key, boolean value) {
		FirebaseCrashlytics.getInstance().setCustomKey(key, value);
	}

	public void setSessionData(String key, String value) {
		FirebaseCrashlytics.getInstance().setCustomKey(key, value);
	}
}
