package com.watabou.pixeldungeon.items.potions;

import com.watabou.pixeldungeon.items.Item;

/**
 * Created by mike on 28.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class UpgradablePotion extends Potion {
	public UpgradablePotion() {
		super();
	}

	@Override
	public boolean isUpgradable() {
		return true;
	}

	double qualityFactor() {
		return Math.pow(level(),1.8);
	}

	@Override
	public Item upgrade() {
		if(quantity() > 1) {
			Item potion = detach(getCurUser().belongings.backpack);
			getCurUser().collect(potion.upgrade());
			return this;
		}
		return super.upgrade();
	}

	@Override
	public Item degrade() {

		if(quantity() > 1) {
			Item potion = detach(getCurUser().belongings.backpack);
			getCurUser().collect(potion.degrade());
			return this;
		}
		return super.degrade();
	}

	@Override
	public int visiblyUpgraded() {
		return level();
	}
}
