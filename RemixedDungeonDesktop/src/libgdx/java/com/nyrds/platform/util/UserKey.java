
package com.nyrds.platform.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class UserKey {
	private static UUID userId;
	private static Crypter crypter;
	private static final String PREFS_FILE = "user_prefs.properties";
	private static final String USER_KEY_PROPERTY = "userKey";
	private static final String NO_KEY = "noKey";

	private static void init() {
		Properties prefs = new Properties();
		try {
			// Load existing preferences
			prefs.load(new FileInputStream(PREFS_FILE));
		} catch (IOException e) {
			// If the file doesn't exist, prefs will be empty
		}

		// Check if userKey exists
		String key = prefs.getProperty(USER_KEY_PROPERTY, NO_KEY);
		if (NO_KEY.equals(key)) {
			// Generate a new UUID
			userId = UUID.randomUUID();
			// Save the new userKey
			prefs.setProperty(USER_KEY_PROPERTY, userId.toString());
			try {
				prefs.store(new FileOutputStream(PREFS_FILE), "User preferences");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// Retrieve the existing UUID
			userId = UUID.fromString(key);
		}

		// Initialize the crypter
		crypter = new Crypter("RPD_UserKey_" + userId.toString());
	}

	public static int someValue() {
		if (userId == null) {
			init();
		}
		return (int) userId.getLeastSignificantBits();
	}

	public static String encrypt(String in) {
		if (crypter == null) {
			init();
		}
		return crypter.encrypt(in);
	}

	public static String decrypt(String in) {
		if (crypter == null) {
			init();
		}
		return crypter.decrypt(in);
	}
}