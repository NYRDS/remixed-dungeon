
package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import org.jetbrains.annotations.NotNull;

public class Blindness extends FlavourBuff {


	@Override
	public boolean attachTo(@NotNull Char target) {
		boolean ret = super.attachTo(target);
		if (ret) {
			target.observe();
		}
		return ret;
	}

	@Override
	public void detach() {
		super.detach();
		target.observe();
	}
	
	@Override
	public int icon() {
		return BuffIndicator.BLINDNESS;
	}
}
