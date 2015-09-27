package com.nyrds.android.util;

public class Scrambler {
	static private int k1 = 983, k2 = 991, k3 = 0xAAAAAAAA;
	
	static public void setKeys(int k1, int k2, int k3) {
		Scrambler.k1 = k1;
		Scrambler.k2 = k2;
		Scrambler.k3 = k3;
	}
	
	static public int scramble(int in) {
		return ( (in + k1) * k2 ) ^ k3;
	}
	
	static public int descramble(int in) {
		return ( in ^ k3 ) / k2 - k1;
	}
	
}
