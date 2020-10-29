package com.watabou.pixeldungeon.items.potions;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Random;

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
		return Math.pow(2,level());
	}

	@Override
	public Item upgrade() {
		if(getOwner().valid() && quantity() > 1) {
			Item potion = detach(getOwner().getBelongings().backpack);
			getOwner().collect(potion.upgrade());
			return this;
		}
		return super.upgrade();
	}

	@Override
	public Item degrade() {

		if(getOwner().valid() && quantity() > 1) {
			Item potion = detach(getOwner().getBelongings().backpack);
			getOwner().collect(potion.degrade());
			return this;
		}
		return super.degrade();
	}

	@Override
	public int visiblyUpgraded() {
		return level();
	}

	@Override
	public Item random() {
		if (Random.Float() < 0.15f) {
			upgrade();
			if (Random.Float() < 0.15f) {
				upgrade();
			}
		}

		return this;
	}
}
