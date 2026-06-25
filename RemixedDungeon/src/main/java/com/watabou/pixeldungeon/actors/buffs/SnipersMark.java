
package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.ui.BuffIndicator;

import com.nyrds.LuaInterface;

@LuaInterface


public class SnipersMark extends FlavourBuff {
	
	@Override
	public int icon() {
		return BuffIndicator.MARK;
	}
}
