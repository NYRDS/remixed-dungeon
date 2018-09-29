package com.nyrds.pixeldungeon.ml;

import android.os.SystemClock;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nyrds.android.util.Util;
import com.watabou.pixeldungeon.Preferences;

import java.util.HashMap;

/**
 * Created by mike on 09.03.2016.
 */
public class EventCollector {
	public static final String BUG = "bug";

	static private Tracker mTracker;
	static private boolean mDisabled = true;

	static private HashMap<String, Long> timings = new HashMap<>();

	private static boolean googleAnalyticsUsable() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_COLLECT_STATS,1) > 0;
	}

	static public void init() {
		if (mTracker == null) {

			if(!googleAnalyticsUsable()) {
				mDisabled = true;
				return;
			}

			AnalyticsTrackers.initialize();

			mTracker = AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
			mTracker.enableAdvertisingIdCollection(true);
			mDisabled = false;
		}
	}

	static public void logEvent(String category, String event) {
		if (!mDisabled) {
			mTracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(event).build());
		}
	}

	static public void logEvent(String category, String event, String label) {
		if (!mDisabled) {
			mTracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(event).setLabel(label).build());
		}
	}

	static public void logScene(String scene) {
		if (!mDisabled) {
			mTracker.setScreenName(scene);
			mTracker.send(new HitBuilders.ScreenViewBuilder().build());
		}
	}

	static public void logException(Exception e) {
		if(!mDisabled) {
			Crashlytics.logException(e);
			mTracker.send(new HitBuilders.ExceptionBuilder().setDescription(Util.toString(e)).build());
		}
	}

	static public void logException(Exception e,String desc) {
		if(!mDisabled) {
			Crashlytics.log(desc);
			Crashlytics.logException(e);
			mTracker.send(new HitBuilders.ExceptionBuilder().setDescription(desc + " "+Util.toString(e)).build());
		}
	}

	static public void startTiming(String id) {
		if(!mDisabled) {
			timings.put(id, SystemClock.elapsedRealtime());
		}
	}

	static public void rawTiming(long value,  String category, String variable, String label) {
		if(!mDisabled) {
			mTracker.send(new HitBuilders.TimingBuilder()
					.setCategory(category)
					.setValue(value)
					.setVariable(variable)
					.setLabel(label)
					.build());
		}
	}

	static public void stopTiming(String id, String category, String variable, String label) {

		if(!mDisabled) {
			long time = SystemClock.elapsedRealtime();
			long delta = time - timings.get(id);

			mTracker.send(new HitBuilders.TimingBuilder()
					.setCategory(category)
					.setValue(delta)
					.setVariable(variable)
					.setLabel(label)
					.build());
		}
	}

	static public void stopTiming(String id) {
		stopTiming(id,"timings",id,"none");
	}

	public static void collectSessionData(String key, String value) {
		Crashlytics.setString(key,value);
	}
}
