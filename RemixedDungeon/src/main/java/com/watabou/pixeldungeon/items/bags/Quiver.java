package com.watabou.pixeldungeon.items.bags;

import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class Quiver extends Bag {

	{
		image = ItemSpriteSheet.QUIVER_COMMON;
	}

	@Override
	public int price() {
		return 50;
	}
}
