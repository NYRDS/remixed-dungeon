package com.nyrds.util;

import android.content.SharedPreferences;

import com.nyrds.platform.storage.Preferences;

import java.util.UUID;

public class UserKey {
	private static UUID userId;

	private static Crypter crypter;

	private static final String noKey="noKey";

	private static void init() {

		SharedPreferences prefs = Preferences.INSTANCE.get();

		String key = prefs.getString("userKey", noKey);
		if(key.equals(noKey)) { 
			userId = UUID.randomUUID();
			
			prefs.edit().putString("userKey", userId.toString()).apply();
		} else {
			userId = UUID.fromString(key);
		}

		crypter = new Crypter("RPD_UserKey_"+userId.toString());
	}

	public static int someValue(){
		if(userId == null){
			init();
		}

		return (int)userId.getLeastSignificantBits();
	}

	public static String encrypt(String in){
		if(crypter == null){
			init();
		}
		return crypter.encrypt(in);
	}
	
	public static String decrypt(String in){
		if(crypter == null){
			init();
		}
		return crypter.decrypt(in);
	}
}
