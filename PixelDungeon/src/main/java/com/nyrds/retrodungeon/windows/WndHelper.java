package com.nyrds.retrodungeon.windows;

/**
 * Created by mike on 04.01.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class WndHelper {
	private static float maxWidth, maxHeight;


	public static void update(float mw, float mh) {
		maxWidth = mw;
		maxHeight = mh;
	}

	public static int getFullscreenWidth() {
		return (int) (maxWidth - 14);
	}

	public static int getFullscreenHeight() {
		return (int) (maxHeight - 14);
	}

	public static int getLimitedWidth(int limit) {
		return Math.min(getFullscreenWidth(),limit);
	}
}
