
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import org.jetbrains.annotations.NotNull;

public class Terror extends FlavourBuff {

	public static final float DURATION = 10f;

	@Override
	public int icon() {
		return BuffIndicator.TERROR;
	}

	public static void recover( Char target ) {
		Terror terror = target.buff( Terror.class );
		if (terror != null && terror.cooldown() < DURATION) {
			target.remove( terror );
		}
	}

	@Override
	public boolean attachTo(@NotNull Char target) {
		if(super.attachTo(target)) {
			if(target instanceof Mob && target.fraction!=Fraction.NEUTRAL) {
				Mob tgt = (Mob)target;
				tgt.releasePet();
			}
			return true;
		}
		return false;
	}

	@Override
	public void attachVisual() {
        target.showStatus(CharSprite.NEGATIVE, StringsManager.getVar(R.string.Char_StaFrightened));
	}
}
