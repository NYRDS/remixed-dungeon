package com.nyrds.util;

import com.nyrds.platform.util.PUtil;

import java.util.UUID;

public class UserKey {
	private static UUID userId;

	private static Crypter crypter;

	private static final String noKey="noKey";

	private static void init() {
		userId = PUtil.getUserId();
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
