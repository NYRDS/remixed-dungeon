package com.nyrds.platform.storage;

import java.util.Map;

public interface IPreferences {
    String getString(String key, String defValue, Map<String, String> stringCache);

    void put(String key, String value, Map<String, String> stringCache);
}
