package com.nyrds.android.util;

import java.util.UUID;

import android.content.SharedPreferences;
import android.util.Log;

import com.watabou.noosa.Game;

public class UserKey {
	static UUID userId;
	
	static Crypter crypter;
	
	static final String noKey="noKey";
	
	private static void init() {
		SharedPreferences prefs = Game.instance().getPreferences( Game.MODE_PRIVATE );
		
		String key = prefs.getString("userKey", noKey);
		if(key.equals(noKey)) { 
			userId = UUID.randomUUID();
			
			prefs.edit().putString("userKey", userId.toString()).commit();
		} else {
			userId = UUID.fromString(key);
		}
		
		Log.d("UserKey", userId.toString());
		
		crypter = new Crypter("RPD_UserKey_"+userId.toString());
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
