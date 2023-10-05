
package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.rings.RingOfElements.Resistance;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import org.jetbrains.annotations.NotNull;

public class Weakness extends FlavourBuff {

	private static final float DURATION = 40f;

	@Override
	public int icon() {
		return BuffIndicator.WEAKNESS;
	}
	
	@Override
	public boolean attachTo(@NotNull Char target ) {
		if (super.attachTo( target )) {
			target.getBelongings().discharge();
			return true;
		} else {
			return false;
		}
	}

	public static float duration( Char ch ) {
		Resistance r = ch.buff( Resistance.class );
		return r != null ? r.durationFactor() * DURATION : DURATION;
	}
}
