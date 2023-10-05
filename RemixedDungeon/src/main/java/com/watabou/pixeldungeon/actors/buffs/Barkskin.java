
package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.ui.BuffIndicator;

public class Barkskin extends Buff {

	@Override
	public boolean act() {
		if (target.isAlive()) {

			spend( TICK );
			if (--level <= 0) {
				detach();
			}
		} else {
			detach();
		}

		return true;
	}

	public void level( int value ) {
		if (level < value) {
			level = value;
		}
	}

	@Override
	public int drBonus() {
		return level();
	}

	@Override
	public int icon() {
		return BuffIndicator.BARKSKIN;
	}
}
