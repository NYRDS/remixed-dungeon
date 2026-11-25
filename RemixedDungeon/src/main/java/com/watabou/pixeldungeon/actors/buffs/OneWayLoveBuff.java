package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.items.rings.ArtifactBuff;
import com.watabou.pixeldungeon.ui.BuffIndicator;

/**
 * DriedRose charm-state buff (was quest.DriedRose.OneWayLoveBuff; kind string
 * kept - CharUtils.charmDurationFactor + scripts/buffs/Charm.lua query it).
 */
public class OneWayLoveBuff extends ArtifactBuff {
	@Override
	public int icon() {
		return BuffIndicator.ROSE;
	}

	@Override
	public String name() {
		return StringsManager.getVar(com.nyrds.pixeldungeon.ml.R.string.DriedRoseBuff_Name);
	}

	@Override
	public String desc() {
		return StringsManager.getVar(com.nyrds.pixeldungeon.ml.R.string.DriedRoseBuff_Info);
	}
}
