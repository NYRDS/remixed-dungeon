package com.nyrds.android.util;

import java.util.UUID;
import android.content.SharedPreferences;
import com.watabou.noosa.Game;

public class UserKey {
	static UUID userId;
	
	static Crypter crypter;
	
	static final String noKey="noKey";
	
	public static void init() {
		SharedPreferences prefs = Game.instance().getPreferences( Game.MODE_PRIVATE );
		
		String key = prefs.getString("userKey", noKey);
		if(key.equals(noKey)) { 
			userId = UUID.randomUUID();
			
			prefs.edit().putString("userKey", userId.toString());
		} else {
			userId = UUID.fromString(key);
		}
		
		crypter = new Crypter("RPD_UserKey_"+userId.toString());
	}
	
	static String encrypt(String in){
		return crypter.encrypt(in);
	}
	
	static String decrypt(String in){
		return crypter.decrypt(in);
	}
}
