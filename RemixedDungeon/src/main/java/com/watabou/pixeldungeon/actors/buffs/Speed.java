
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.LuaInterface;
import com.watabou.pixeldungeon.actors.Char;

@LuaInterface
public class Speed extends FlavourBuff {
	
	public static final float DURATION = 10f;

	@Override
	public float hasteLevel(Char chr) {
		return 7.27254f;
	}
}
