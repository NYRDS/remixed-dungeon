package com.nyrds.pixeldungeon.ml;

import android.os.Build;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Preferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 09.03.2016.
 */
public class EventCollector {
    public static final String SAVE_ADS_EXPERIMENT = "SaveAdsExperiment2";

    static private FirebaseAnalytics mFirebaseAnalytics;
	static private boolean mDisabled = true;

	static private HashMap<String,Trace> timings = new HashMap<>();

	private static boolean analyticsUsable() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_COLLECT_STATS,1) > 0 && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);
	}

	static public void init() {
	    if(analyticsUsable()) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(RemixedDungeonApp.getContext());
            mDisabled = false;
        }
	}

	static public void logEvent(String category, String event) {
		if (!mDisabled) {
			Crashlytics.log(category+":"+event);

			Bundle params = new Bundle();
			params.putString("event", event);
			mFirebaseAnalytics.logEvent(category, params);
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
			Crashlytics.log(category+":"+event+":"+label);

			Bundle params = new Bundle();
			params.putString("event", event);
			params.putString("label", label);
			mFirebaseAnalytics.logEvent(category, params);
		}
	}

	static public void logScene(final String scene) {
		if (!mDisabled) {
			Game.instance().runOnUiThread(() -> mFirebaseAnalytics.setCurrentScreen(Game.instance(), scene, null));
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
			Crashlytics.logException(e);
		}
	}

	static public void logException(Throwable e) {
		logException(e,2);
	}

	static public void logException(Throwable e, String desc) {
		if(!mDisabled) {
			Crashlytics.log(desc);
			logException(e, 2);
		}
	}

	static public void startTrace(String id) {
		if(!mDisabled) {
			Trace trace = FirebasePerformance.getInstance().newTrace(id);
			trace.start();
			timings.put(id,trace);
		}
	}

	static public void stopTrace(String id, String category, String variable, String label) {

		if(!mDisabled) {

		    Trace trace = timings.get(id);
			if (trace==null) {
				logException("attempt to stop null timer:"+id);
				return;
			}

		    trace.putAttribute("category", category);
            trace.putAttribute("variable", variable);
            trace.putAttribute("label",    label);

		    trace.stop();
		    timings.remove(id);
		}
	}

	public static void collectSessionData(String key, String value) {
		Crashlytics.setString(key,value);
	}
}
