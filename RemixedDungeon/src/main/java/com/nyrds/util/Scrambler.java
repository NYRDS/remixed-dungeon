package com.nyrds.util;

public class Scrambler {
	static private final int k = 0xAAAAAAAA;

	
	static public int scramble(int in) {
		if(Util.isDebug()) {
			return in;
		}
		return in  ^ k ^ UserKey.someValue();
	}
	
	static public int descramble(int in) {
		if(Util.isDebug()) {
			return in;
		}
		return in ^ UserKey.someValue() ^ k;
	}
	
}
