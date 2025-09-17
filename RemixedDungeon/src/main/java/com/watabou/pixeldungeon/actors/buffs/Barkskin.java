
package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class Barkskin extends Buff {

	@Override
	public boolean act() {
		if (target.isAlive()) {

			spend( TICK );
			if (--buffLevel <= 0) {
				detach();
			}
		} else {
			detach();
		}

		return true;
	}

	public void level( int value ) {
		if (buffLevel < value) {
			buffLevel = value;
		}
	}

	@Override
	public int drBonus(Char chr) {
		return level();
	}

	@Override
	public int icon() {
		return BuffIndicator.BARKSKIN;
	}
}
