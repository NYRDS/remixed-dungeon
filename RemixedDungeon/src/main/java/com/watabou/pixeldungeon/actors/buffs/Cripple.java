
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class Cripple extends FlavourBuff {

	public static final float DURATION	= 10f;
	
	@Override
	public int icon() {
		return BuffIndicator.CRIPPLE;
	}

	@Override
	public float speedMultiplier() {
		return 0.5f;
	}

	@Override
	public void attachVisual() {
        target.showStatus(CharSprite.NEGATIVE, StringsManager.getVar(R.string.Char_StaCrippled));
	}
}
