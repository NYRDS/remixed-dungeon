
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class Amok extends FlavourBuff {
	
	@Override
	public int icon() {
		return BuffIndicator.AMOK;
	}

	@Override
	public void attachVisual() {
        target.showStatus(CharSprite.NEGATIVE, StringsManager.getVar(R.string.Char_StaAmok));
	}
}
