

package com.watabou.utils;

public class SystemTime {

	private static long now;
	private static long lastActionTime;

	public static void tick() {
		now = System.currentTimeMillis();
	}

	public static long now() {
		return now;
	}

	public static long timeSinceTick() {
		return System.currentTimeMillis() - now;
	}

	public static long getLastActionTime() {
		return lastActionTime;
	}

	public static void updateLastActionTime() {
		lastActionTime = now();
	}
}

