package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class Blessed extends FlavourBuff {

	@Packable
	protected int level;
	

	public void set( int level ) {
		this.level = level;
	}
	
	@Override
	public int icon() {
		return BuffIndicator.BLEESSED;
	}
	
	@Override
	public String toString() {
		return Game.getVar(R.string.Blessed_Info);
	}

	@Override
	public int level() {
		return level;
	}
}
