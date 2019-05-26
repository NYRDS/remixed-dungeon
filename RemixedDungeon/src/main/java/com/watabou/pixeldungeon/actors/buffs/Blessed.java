package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class Blessed extends FlavourBuff {

	@Override
	public int icon() {
		return BuffIndicator.BLEESSED;
	}
	
	@Override
	public String name() {
		return Game.getVar(R.string.Blessed_Info);
	}
}
