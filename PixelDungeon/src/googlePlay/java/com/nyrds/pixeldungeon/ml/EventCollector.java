package com.nyrds.pixeldungeon.ml;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Preferences;

import java.util.HashMap;

/**
 * Created by mike on 09.03.2016.
 */
public class EventCollector {
	public static final String BUG = "bug";


	static private FirebaseAnalytics mFirebaseAnalytics;

	static private boolean mDisabled = true;

	static private HashMap<String, Long> timings;

	private static boolean analyticsUsable() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_COLLECT_STATS,100) > 0;
	}

	static public void init(Context context) {
			if(!analyticsUsable()) {
				EventCollector.disable();
				return;
			}
			mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
	}

	private static void disable() {
		mDisabled = true;
	}

	static public void logEvent(String category, String event) {
		if (!mDisabled) {
			Bundle bundle = new Bundle();
			bundle.putString("event", event);
			mFirebaseAnalytics.logEvent(category,bundle);
		}
	}

	static public void logEvent(String category, String event, String label) {
		if (!mDisabled) {
			Bundle bundle = new Bundle();
			bundle.putString("event", event);
			bundle.putString("label", label);
			mFirebaseAnalytics.logEvent(category,bundle);
		}
	}

	static public void logScene(String scene) {
		if (!mDisabled) {
			mFirebaseAnalytics.setCurrentScreen(Game.instance(),scene,null);
		}
	}

	static public void logException(Exception e) {
		if(!mDisabled) {
			FirebaseCrash.report(e);
		}
	}

	static public void logException(Exception e,String desc) {
		if(!mDisabled) {
			FirebaseCrash.log(desc);
			FirebaseCrash.report(e);
		}
	}

	static public void startTiming(String id) {

	}

	static public void stopTiming(String id) {

	}
}
