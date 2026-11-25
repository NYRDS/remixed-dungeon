package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.items.rings.ArtifactBuff;
import com.watabou.pixeldungeon.ui.BuffIndicator;

/**
 * RatSkull aura buff (was quest.RatSkull.RatterAura).
 */
public class RatterAura extends ArtifactBuff {
	@Override
	public String name() {
		return StringsManager.getVar(com.nyrds.pixeldungeon.ml.R.string.RatSkullBuff_Name);
	}

	@Override
	public String desc() {
		return StringsManager.getVar(com.nyrds.pixeldungeon.ml.R.string.RatSkullBuff_Info);
	}

	@Override
	public int icon() {
		return BuffIndicator.RAT_SKULL;
	}
}
