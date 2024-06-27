package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.ui.BuffIndicator;

public class Blessed extends FlavourBuff {

	@Override
	public int icon() {
		return BuffIndicator.BLEESSED;
	}

	@Override
	public int defenceSkillBonus() {
		return level();
	}

	@Override
	public int attackSkillBonus() {
		return level();
	}
}
