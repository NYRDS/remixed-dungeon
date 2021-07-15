package com.nyrds.util;

import com.nyrds.platform.game.Game;

/**
 * Created by mike on 14.08.2016.
 */
public class GuiProperties {
	public static int regularFontSize() {
		if(Game.smallResScreen()) {
			return 8;
		} else {
			return 7;
		}
	}

	public static int titleFontSize() {
		return 9;
	}

	public static int smallFontSize() {
		if(Game.smallResScreen()) {
			return 8;
		} else {
			return 6;
		}
	}

	public static float bigTitleFontSize() {
		return 16;
	}

	public static float mediumTitleFontSize() {
		return 12;
	}
}
