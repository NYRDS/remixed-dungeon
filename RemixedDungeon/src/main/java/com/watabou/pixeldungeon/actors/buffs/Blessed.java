package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class Blessed extends FlavourBuff {

	@Override
	public int icon() {
		return BuffIndicator.BLEESSED;
	}
	
	@Override
	public String name() {
        return StringsManager.getVar(R.string.BlessedBuff_Name);
    }

	@Override
	public String desc() {
        return StringsManager.getVar(R.string.BlessedBuff_Info);
    }
}
