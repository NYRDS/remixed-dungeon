
package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.utils.Utils;

//Special kind of buff, that doesn't perform any kind actions
public class FlavourBuff extends Buff {

	@Override
	public String desc() {
		String ret = super.desc();
		ret += Utils.format("\ncooldown %.2f", cooldown());
		return ret;
	}
	@Override
	public boolean act() {
		detach();
		return true;
	}
}
