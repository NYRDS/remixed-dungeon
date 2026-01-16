

package com.watabou.utils;

import com.nyrds.pixeldungeon.game.GameLoop;

public class GameMath {
	
	public static float speed( float speed, float acc ) {
		return speed + acc * GameLoop.elapsed;
	}
	
	public static float gate( float min, float value, float max ) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}
}
