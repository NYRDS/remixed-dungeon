
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import org.jetbrains.annotations.NotNull;

public class Roots extends FlavourBuff {
	
	@Override
	public boolean attachTo(@NotNull Char target ) {
		if (target.isFlying()) {
			return false;
		}

        return super.attachTo(target);
	}

	@Override
	public int icon() {
		return BuffIndicator.ROOTS;
	}

	@Override
	public void attachVisual() {
        target.showStatus(CharSprite.NEGATIVE, StringsManager.getVar(R.string.Char_StaRooted));
	}
}
