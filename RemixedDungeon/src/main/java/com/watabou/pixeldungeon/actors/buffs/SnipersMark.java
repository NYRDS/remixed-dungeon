
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.LuaInterface;
import com.watabou.pixeldungeon.ui.BuffIndicator;

@LuaInterface
public class SnipersMark extends FlavourBuff {
	
	@Override
	public int icon() {
		return BuffIndicator.MARK;
	}
}
