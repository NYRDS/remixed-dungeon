
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.util.Util;
import com.watabou.pixeldungeon.utils.Utils;

//Special kind of buff, that doesn't perform any kind actions
public class FlavourBuff extends Buff {

	@Override
	public String desc() {
		String ret = super.desc();
		float cooldown = cooldown();
		if (cooldown <= Util.BIG_FLOAT / 2) {
			ret += Utils.format("\ncooldown %.2f", cooldown());
		}
		return ret;
	}
	@Override
	public boolean act() {
		detach();
		return true;
	}
}
