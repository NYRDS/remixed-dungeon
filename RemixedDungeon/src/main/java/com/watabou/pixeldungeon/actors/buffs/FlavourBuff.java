
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.utils.Utils;

//Special kind of buff, that doesn't perform any kind actions
public class FlavourBuff extends Buff {

	@Override
	public String desc() {
		String ret = super.desc();
		float cooldown = cooldown();
		if (cooldown <= Util.BIG_FLOAT / 2) {
			ret += "\n";
			ret += Utils.format(R.string.BuffParam_Cooldown, cooldown());
		}
		return ret;
	}
	@Override
	public boolean act() {
		detach();
		return true;
	}
}
