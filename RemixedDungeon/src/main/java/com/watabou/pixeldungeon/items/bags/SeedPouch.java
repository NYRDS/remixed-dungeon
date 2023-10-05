
package com.watabou.pixeldungeon.items.bags;

import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class SeedPouch extends Bag {

	{
		image = ItemSpriteSheet.POUCH;
	}

	@Override
	public int price() {
		return 50;
	}
}
