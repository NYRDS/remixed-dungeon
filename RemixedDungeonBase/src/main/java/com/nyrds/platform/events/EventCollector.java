package com.nyrds.platform.events;

import java.util.Map;

/**
 * Created by mike on 09.03.2016.
 */

public class EventCollector {

	static private IEventCollector impl;

	static public void init(IEventCollector impl) {
		EventCollector.impl = impl;
	}

	static public void logCountedEvent(String event, int threshold) {
		if(EventCollector.impl != null) {
			EventCollector.impl.logCountedEvent(event, threshold);
		}
	}
	static public void logEvent(String event) {
		if (EventCollector.impl != null) {
			EventCollector.impl.logEvent(event);
		}
	}

	static public void logEvent(String event, double value) {
		if (EventCollector.impl != null) {
			EventCollector.impl.logEvent(event, value);
		}
	}

	static public void logEvent(String category, String event) {
		if(EventCollector.impl != null) {
			EventCollector.impl.logEvent(category, event);
		}
	}

	static public void levelUp(String character, long level) {
		if(EventCollector.impl != null) {
			EventCollector.impl.levelUp(character, level);
		}
	}

	static public void badgeUnlocked(String badgeId) {
		if(EventCollector.impl != null) {
			EventCollector.impl.badgeUnlocked(badgeId);
		}
	}

	static public void logEvent(String category, Map<String,String> eventData) {
		if(EventCollector.impl != null) {
			EventCollector.impl.logEvent(category, eventData);
		}
	}

	static public void logEvent(String category, String event, String label) {
		if(EventCollector.impl != null) {
			EventCollector.impl.logEvent(category, event, label);
		}
	}

	static public void logScene(final String scene) {
		if(EventCollector.impl != null) {
			EventCollector.impl.logScene(scene);
		}
	}

	static public void logException() {
		logException(new Exception(),1);
	}

	static public void logException(String desc) {
		logException(new Exception(desc),1);
	}

	static private void logException(Throwable e, int level) {
		if(EventCollector.impl != null) {
			EventCollector.impl.logException(e, level);
		}
	}

	static public void logException(Throwable e) {
		logException(e,0);
	}

	static public void logException(Throwable e, String desc) {
		if (EventCollector.impl != null) {
			EventCollector.impl.logException(e, desc);
		}
	}

	public static void setSessionData(String key, boolean value) {
		if(EventCollector.impl != null) {
			EventCollector.impl.setSessionData(key, value);
		}
	}

	public static void setSessionData(String key, String value) {
		if(EventCollector.impl != null) {
			EventCollector.impl.setSessionData(key, value);
		}
	}
}
