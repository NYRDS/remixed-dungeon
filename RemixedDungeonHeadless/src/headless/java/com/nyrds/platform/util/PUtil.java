package com.nyrds.platform.util;

import java.util.UUID;

// headless: plain stdout logging
public class PUtil {

	static public boolean isConnectedToNetwork() {
		return false;
	}

	static public boolean isConnectedToInternet() {
		return false;
	}

	static public void slog(String tag, String txt) {
		System.out.println("[" + tag + "] " + txt);
	}

	static public UUID getUserId() {
		return UUID.randomUUID();
	}

	public static long getAvailableInternalMemorySize() {
		return 1024 * 1024 * 1024;
	}

	public static void gcHint() {
	}
}
