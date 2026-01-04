package com.watabou.pixeldungeon.items.bags;

import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class Keyring extends Bag {

	{
		image = ItemSpriteSheet.KEYRING;
	}

	@Override
	public int price() {
		return 50;
	}
}
