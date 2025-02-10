package com.nyrds.platform;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;
import com.nyrds.platform.util.PUtil;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.HashMap;
import java.util.Map;

public class RemoteConfig {

    private final FirebaseRemoteConfig firebaseRemoteConfig;
    private static RemoteConfig instance;
    private final SharedPreferences sharedPreferences;

    // Keys for SharedPreferences
    private static final String PREFS_NAME = "FirebaseRemoteConfigPrefs";

    // Private constructor to enforce singleton pattern
    private RemoteConfig(Context context) {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize Firebase Remote Config automatically
        initialize();
    }

    // Singleton instance with automatic initialization
    public static synchronized RemoteConfig getInstance(Context context) {
        if (instance == null) {
            instance = new RemoteConfig(context);
        }
        return instance;
    }

    // Initialize Firebase Remote Config with cached values from SharedPreferences
    private void initialize() {
        // Load cached values from SharedPreferences
        Map<String, Object> cachedConfigs = loadCachedConfigs();

        // Set cached values as defaults
        firebaseRemoteConfig.setDefaultsAsync(cachedConfigs);

        // Enable developer mode for faster fetching (optional)
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // Default fetch interval (1 hour)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        // Fetch and cache the config automatically
        fetchAndCacheConfig();
    }

    // Load cached values from SharedPreferences
    private Map<String, Object> loadCachedConfigs() {
        Map<String, Object> cachedConfigs = new HashMap<>();

        // Iterate through all keys in SharedPreferences
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Add the cached value to the map
            cachedConfigs.put(key, value);
        }

        return cachedConfigs;
    }

    // Fetch and cache the config
    private void fetchAndCacheConfig() {
        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            PUtil.slog("FirebaseRemoteConfig", "Config fetched and activated");
                            cacheAllConfigValues(); // Cache all config values
                        } else {
                            PUtil.slog("FirebaseRemoteConfig", "Fetch failed, using cached values");
                        }
                    }
                });
    }

    // Cache all config values in SharedPreferences
    private void cacheAllConfigValues() {
        Map<String, FirebaseRemoteConfigValue> configValues = firebaseRemoteConfig.getAll();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (Map.Entry<String, FirebaseRemoteConfigValue> entry : configValues.entrySet()) {
            String key = entry.getKey();
            FirebaseRemoteConfigValue configValue = entry.getValue();

            // Extract the value based on its type
            try {
                // Try to get the value as a boolean
                try {
                    boolean booleanValue = configValue.asBoolean();
                    editor.putBoolean(key, booleanValue);
                    continue; // Successfully cached as boolean, move to the next key
                } catch (IllegalArgumentException e) {
                    // Not a boolean, try the next type
                }

                // Try to get the value as a long
                try {
                    long longValue = configValue.asLong();
                    editor.putLong(key, longValue);
                    continue; // Successfully cached as long, move to the next key
                } catch (IllegalArgumentException e) {
                    // Not a long, try the next type
                }

                // Try to get the value as a double
                try {
                    double doubleValue = configValue.asDouble();
                    editor.putFloat(key, (float) doubleValue); // Store as float in SharedPreferences
                    continue; // Successfully cached as double (stored as float), move to the next key
                } catch (IllegalArgumentException e) {
                    // Not a double, try the next type
                }

                // Try to get the value as a string
                String stringValue = configValue.asString();
                editor.putString(key, stringValue); // Store as string

            } catch (Exception e) {
                PUtil.slog("RemoteConfig", "Error caching value for key: " + key + ", error: " + e.getMessage());
            }
        }

        editor.apply();
    }

    // Get the cached value for a specific parameter (Boolean)
    public boolean getBoolean(String parameterName, boolean defaultValue) {
        boolean ret = sharedPreferences.getBoolean(parameterName, defaultValue);
        GLog.debug("RemoteConfig: getBoolean: " + parameterName + " = " + ret);
        return ret;
    }

    // Get the cached value for a specific parameter (String)
    public String getString(String parameterName, String defaultValue) {
        return sharedPreferences.getString(parameterName, defaultValue);
    }

    // Get the cached value for a specific parameter (Long)
    public long getLong(String parameterName, long defaultValue) {
        return sharedPreferences.getLong(parameterName, defaultValue);
    }

    // Get the cached value for a specific parameter (Float)
    public float getFloat(String parameterName, float defaultValue) {
        return sharedPreferences.getFloat(parameterName, defaultValue);
    }

    // Force a fresh fetch of the config (e.g., for manual refresh)
    public void forceFetchConfig(final FirebaseRemoteConfigListener listener) {
        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            cacheAllConfigValues(); // Cache all config values
                            if (listener != null) {
                                listener.onConfigFetched(true);
                            }
                        } else {
                            if (listener != null) {
                                listener.onFetchFailed();
                            }
                        }
                    }
                });
    }

    // Interface for callback (optional, for manual refresh)
    public interface FirebaseRemoteConfigListener {
        void onConfigFetched(boolean success);
        void onFetchFailed();
    }
}