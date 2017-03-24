package com.nyrds.pixeldungeon.items.common;

import com.watabou.pixeldungeon.items.Item;

public class RatHide extends Item {


	public RatHide() {
		imageFile = "items/artifacts.png";
		image = 23;
		stackable = true;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

}
