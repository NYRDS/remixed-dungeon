package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class Blessed extends FlavourBuff {

	@Override
	public int icon() {
		return BuffIndicator.BLEESSED;
	}

	@Override
	public int defenceSkillBonus(Char chr) {
		return level();
	}

	@Override
	public int attackSkillBonus(Char chr) {
		return level();
	}
}
