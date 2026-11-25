package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.items.rings.ArtifactBuff;
import com.watabou.pixeldungeon.ui.BuffIndicator;

/**
 * Cursed DriedRose charm-state buff (was quest.DriedRose.OneWayCursedLoveBuff;
 * kind string kept - CharUtils.charmDurationFactor queries it).
 */
public class OneWayCursedLoveBuff extends ArtifactBuff {
	@Override
	public int icon() {
		return BuffIndicator.CURSED_ROSE;
	}

	@Override
	public String name() {
		return StringsManager.getVar(com.nyrds.pixeldungeon.ml.R.string.DriedRoseCursedBuff_Name);
	}

	@Override
	public String desc() {
		return StringsManager.getVar(com.nyrds.pixeldungeon.ml.R.string.DriedRoseCursedBuff_Info);
	}
}
