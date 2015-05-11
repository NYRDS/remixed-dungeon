package com.nyrds.android.util;

public class ModdingMode {
	static private boolean mMode = false;

	public static boolean mode() {
		return mMode;
	}

	public static void mode(boolean mMode) {
		ModdingMode.mMode = mMode;
	}

}
