
package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.Dungeon;

public class Awareness extends FlavourBuff {

	public static final float DURATION = 2f;

	@Override
	public void detach() {
		super.detach();
		Dungeon.observe();
	}
}
