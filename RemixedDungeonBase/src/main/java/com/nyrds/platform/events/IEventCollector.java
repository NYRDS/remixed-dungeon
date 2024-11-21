package com.nyrds.platform.events;

import java.util.Map;

public interface IEventCollector {
    void logCountedEvent(String event, int threshold);

    void logEvent(String event);

    void logEvent(String event, double value);

    void logEvent(String category, String event);

    void levelUp(String character, long level);

    void badgeUnlocked(String badgeId);

    void logEvent(String category, Map<String, String> eventData);

    void logEvent(String category, String event, String label);

    void logScene(final String scene);

    void logException(Throwable e, String desc);
    void logException(Throwable e, int level);

    void setSessionData(String key, boolean value);

    void setSessionData(String key, String value);
}
