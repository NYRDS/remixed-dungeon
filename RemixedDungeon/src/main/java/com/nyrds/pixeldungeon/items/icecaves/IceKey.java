package com.nyrds.pixeldungeon.items.icecaves;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;

public class IceKey extends Item {


	public IceKey() {
		imageFile = "items/artifacts.png";
		image = 22;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public String bag() {
		return Bag.KEYRING;
	}
}
